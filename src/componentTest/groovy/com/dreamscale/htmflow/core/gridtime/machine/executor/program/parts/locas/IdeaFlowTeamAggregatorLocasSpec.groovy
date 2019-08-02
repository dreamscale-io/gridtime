package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.CircleMessageType
import com.dreamscale.htmflow.api.team.TeamDto
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.member.OrganizationRepository
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity
import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsRepository
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.htmflow.core.gridtime.machine.Torchie
import com.dreamscale.htmflow.core.gridtime.machine.TorchieFactory
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.IdeaFlowTeamAggregatorLocas
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed
import com.dreamscale.htmflow.core.service.TeamService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.sql.Timestamp
import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

@ComponentTest
class IdeaFlowTeamAggregatorLocasSpec extends Specification {

    @Autowired
    LocasFactory locasFactory

    @Autowired
    GridIdeaFlowMetricsRepository gridMetricsIdeaFlowRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TeamService teamService

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    TorchieFactory torchieFactory

    LocalDateTime clockStart
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    IdeaFlowTeamAggregatorLocas ideaFlowTeamAggregatorLocas
    Metronome metronome

    OrganizationEntity org
    OrganizationMemberEntity member1
    OrganizationMemberEntity member2
    OrganizationMemberEntity member3

    TeamDto team

    def setup() {

        org = aRandom.organizationEntity().save()
        member1 = createMembership(org.getId(), UUID.randomUUID())
        member2 = createMembership(org.getId(), UUID.randomUUID())
        member3 = createMembership(org.getId(), UUID.randomUUID())

        team = teamService.createTeam(org.getId(), "Phoenix23")

        teamService.addMembersToTeam(org.getId(), team.getId(), extractMemberIds(member1, member2, member3))

        ideaFlowTeamAggregatorLocas = locasFactory.createIdeaFlowTeamAggregatorLocas(team.getId());

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(45)
        time3 = clockStart.plusMinutes(60)
        time4 = clockStart.plusMinutes(95)

        Metronome.Tick tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

    }


    def "should aggregate IdeaFlowMetrics by GridTime across Team 23"() {

        given:

        Torchie torchie1 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);
        Torchie torchie2 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);
        Torchie torchie3 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);

        Torchie teamTorchie = torchieFactory.wireUpTeamTorchie(team.id)

        InputFeed feed1 = torchie1.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed1.addSomeData(generateWTFStart(time1))
        feed1.addSomeData(generateWTFEnd(time2))

        InputFeed feed2 = torchie2.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed2.addSomeData(generateWTFStart(time1))
        feed2.addSomeData(generateWTFEnd(time2))

        InputFeed feed3 = torchie3.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed3.addSomeData(generateWTFStart(time1))
        feed3.addSomeData(generateWTFEnd(time2))

        for (int i = 0; i < 12; i++) {
            torchie1.whatsNext().call()
            torchie2.whatsNext().call()
            torchie3.whatsNext().call()
        }

        when:
        TileInstructions instruction = teamTorchie.whatsNext();
        instruction.call();

        then:
        assert instruction != null
        println instruction.getOutputResult()

    }

    def generateWTFStart(LocalDateTime startTime) {
        CircleFeedMessageEntity wtfMessage = new CircleFeedMessageEntity()
        wtfMessage.setMessageType(CircleMessageType.CIRCLE_START)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircleId(UUID.randomUUID())

        return new FlowableCircleMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        CircleFeedMessageEntity wtfMessage = new CircleFeedMessageEntity()
        wtfMessage.setMessageType(CircleMessageType.CIRCLE_CLOSED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircleId(UUID.randomUUID())

        return new FlowableCircleMessageEvent(wtfMessage)
    }


    private List<UUID> extractMemberIds(OrganizationMemberEntity ... members) {
        List<UUID> memberIds = new ArrayList<>();
        for (OrganizationMemberEntity member: members) {
            memberIds.add(member.getId());
        }
        return memberIds;
    }


    private OrganizationMemberEntity createMembership(UUID organizationId, UUID masterAccountId) {
        aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(masterAccountId)
                .save()
    }
}
