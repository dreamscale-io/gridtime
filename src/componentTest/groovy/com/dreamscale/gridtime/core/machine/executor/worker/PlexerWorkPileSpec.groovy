package com.dreamscale.gridtime.core.machine.executor.worker

import com.dreamscale.gridtime.ComponentTest
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.RootAccountEntity
import com.dreamscale.gridtime.core.domain.member.TeamEntity
import com.dreamscale.gridtime.core.domain.member.TeamMemberEntity
import com.dreamscale.gridtime.core.domain.work.WorkToDoType
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.CoordinateResults
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class PlexerWorkPileSpec extends Specification {

    @Autowired
    SystemWorkPile systemWorkPile;

    @Autowired
    PlexerWorkPile plexerWorkPile

    @Autowired
    CircuitActivityDashboard dashboard

    @Autowired
    AggregateWorkToDoQueueWire wire

    @Autowired
    GridClock mockGridClock

    @Autowired
    RootAccountEntity loggedInUser

    UUID torchieId
    UUID teamId

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    OrganizationEntity org

    TeamEntity team

    def setup() {

        torchieId = UUID.randomUUID()
        teamId = UUID.randomUUID()

        clockStart = LocalDateTime.now()
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        mockGridClock.now() >> clockStart

        org = aRandom.organizationEntity().save()
        team = aRandom.teamEntity().id(teamId).organizationId(org.id).save()

        systemWorkPile.reset()
        plexerWorkPile.reset()
    }

    def "should spin up 5 worker threads that pull work based on a queue"() {
        given:

        OrganizationMemberEntity member = createMemberWithinOrgAndTeam();
        loggedInUser.setId(member.getRootAccountId())

        //change active logged in user to a different user within same organization
        OrganizationMemberEntity otherMember =  createMemberWithinOrgAndTeam();

        systemWorkPile.sync()
        TickInstructions calendarInstruction = systemWorkPile.whatsNext().call();

        CoordinateResults coordinates = (CoordinateResults) calendarInstruction.getOutputResult();

        wire.push(new TileStreamEvent(team.id, member.id, coordinates.getGridtime(), WorkToDoType.AggregateToTeam))
        wire.push(new TileStreamEvent(team.id, otherMember.id, coordinates.getGridtime(), WorkToDoType.AggregateToTeam))

        when:

        TickInstructions aggregateInstruction = plexerWorkPile.whatsNext().call();

        then:
        assert aggregateInstruction != null

        assert plexerWorkPile.size() == 5
        assert !plexerWorkPile.hasWork()
    }

    private OrganizationMemberEntity createMemberWithinOrgAndTeam() {

        RootAccountEntity account = aRandom.rootAccountEntity().save()

        OrganizationMemberEntity member = aRandom.memberEntity().organizationId(org.id).rootAccountId(account.id).save()
        TeamMemberEntity teamMember = aRandom.teamMemberEntity().teamId(teamId).organizationId(org.id).memberId(member.id).save()

        return member;

    }

}
