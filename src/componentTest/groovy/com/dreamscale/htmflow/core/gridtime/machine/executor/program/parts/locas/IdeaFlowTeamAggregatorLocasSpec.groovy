package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas

import com.dreamscale.htmflow.ComponentTest
import com.dreamscale.htmflow.api.circle.CircleMessageType
import com.dreamscale.htmflow.api.team.TeamDto
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberRepository
import com.dreamscale.htmflow.core.domain.member.OrganizationRepository
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
    LocalDateTime wtfTime
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
        member1 = createMembership(org.getId())
        member2 = createMembership(org.getId())
        member3 = createMembership(org.getId())

        team = teamService.createTeam(org.getId(), "Team23")

        teamService.addMembersToTeam(org.getId(), team.getId(), extractMemberIds(member1, member2, member3))

        ideaFlowTeamAggregatorLocas = locasFactory.createIdeaFlowTeamAggregatorLocas(team.getId());

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        metronome = new Metronome(clockStart)

        wtfTime = clockStart.plusMinutes(1)

        Metronome.Tick tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

    }


    def "should aggregate IdeaFlowMetrics by GridTime across Team23"() {

        given:

        Torchie torchie1 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);
        Torchie torchie2 = torchieFactory.wireUpMemberTorchie(team.id, member2.getId(), clockStart);
        Torchie torchie3 = torchieFactory.wireUpMemberTorchie(team.id, member3.getId(), clockStart);

        Torchie teamTorchie = torchieFactory.wireUpTeamTorchie(team.id)

        InputFeed feed1 = torchie1.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed1.addSomeData(generateWTFStart(wtfTime))
        feed1.addSomeData(generateWTFEnd(wtfTime.plusMinutes(5)))

        InputFeed feed2 = torchie2.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed2.addSomeData(generateWTFStart(wtfTime))
        feed2.addSomeData(generateWTFEnd(wtfTime.plusMinutes(15)))

        InputFeed feed3 = torchie3.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed3.addSomeData(generateWTFStart(wtfTime))
        feed3.addSomeData(generateWTFEnd(wtfTime.plusMinutes(45)))

        torchie1.whatsNext().call()
        torchie2.whatsNext().call()
        torchie3.whatsNext().call()

        when:
        TileInstructions instruction = teamTorchie.whatsNext();
        instruction.call();

        then:
        assert instruction != null
        MusicGridResults results = instruction.getOutputResult()

        println results

        assert results.getCell("@zoom/wtf", "Calc[Avg]") == "0.68"

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


    private OrganizationMemberEntity createMembership(UUID organizationId) {
        def account = aRandom.masterAccountEntity().save()

        aRandom.memberEntity()
                .organizationId(organizationId)
                .masterAccountId(account.id)
                .save()
    }
}
