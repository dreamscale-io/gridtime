package com.dreamscale.gridtime.core.machine.executor.program.parts.locas

import com.dreamscale.gridtime.ComponentTest

import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.gridtime.core.machine.GridTimeWorkerPool
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.clock.Metronome
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.WorkToDoQueueWire
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableTeamIdeaFlowLocas
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed
import com.dreamscale.gridtime.core.capability.directory.TeamDirectoryCapability
import com.dreamscale.gridtime.core.service.TimeService
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class ZoomableTeamIdeaFlowLocasSpec extends Specification {

    @Autowired
    LocasFactory locasFactory

    @Autowired
    GridIdeaFlowMetricsRepository gridMetricsIdeaFlowRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TeamDirectoryCapability teamService

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    TorchieFactory torchieFactory

    @Autowired
    TimeService mockTimeService

    @Autowired
    ProgramFactory programFactory

    @Autowired
    WorkToDoQueueWire workToDoQueueWire

    @Autowired
    FeatureCacheManager featureCacheManager

    @Autowired
    GridTimeWorkerPool gridTimeWorkerPool

    LocalDateTime clockStart
    LocalDateTime wtfTime
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    ZoomableTeamIdeaFlowLocas ideaFlowTeamAggregatorLocas
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

        Metronome.TickScope tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

        mockTimeService.now() >> LocalDateTime.now()

        gridTimeWorkerPool.clear();

    }


    def "should aggregate IdeaFlowMetrics by GridTime across Team23"() {

        given:

        Torchie torchie1 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);
        Torchie torchie2 = torchieFactory.wireUpMemberTorchie(team.id, member2.getId(), clockStart);
        Torchie torchie3 = torchieFactory.wireUpMemberTorchie(team.id, member3.getId(), clockStart);

        gridTimeWorkerPool.submitJob(torchie1);
        gridTimeWorkerPool.submitJob(torchie2);
        gridTimeWorkerPool.submitJob(torchie3);

        InputFeed feed1 = torchie1.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed1.addSomeData(generateWTFStart(wtfTime))
        feed1.addSomeData(generateWTFEnd(wtfTime.plusMinutes(5)))

        InputFeed feed2 = torchie2.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed2.addSomeData(generateWTFStart(wtfTime))
        feed2.addSomeData(generateWTFEnd(wtfTime.plusMinutes(15)))

        InputFeed feed3 = torchie3.getInputFeed(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED)
        feed3.addSomeData(generateWTFStart(wtfTime))
        feed3.addSomeData(generateWTFEnd(wtfTime.plusMinutes(45)))

        when:

        gridTimeWorkerPool.whatsNext().call();
        gridTimeWorkerPool.whatsNext().call();
        gridTimeWorkerPool.whatsNext().call();

        //last exec should call team tile

        TileInstructions teamInstruction = gridTimeWorkerPool.whatsNext();

        println "RUNNING LAST: "+ teamInstruction.getCmdDescription()
        teamInstruction.call()

        then:
        MusicGridResults results = (MusicGridResults) teamInstruction.getOutputResult()
        println results
        assert results != null
        assert results.getCell("@zoom/wtf", "Calc[Avg]") == "0.68"

    }

    def generateWTFStart(LocalDateTime startTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.CIRCUIT_OPEN)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.CIRCUIT_CLOSED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }


    private List<UUID> extractMemberIds(OrganizationMemberEntity ... members) {
        List<UUID> memberIds = new ArrayList<>();
        for (OrganizationMemberEntity member: members) {
            memberIds.add(member.getId());
        }
        return memberIds;
    }


    private OrganizationMemberEntity createMembership(UUID organizationId) {
        def account = aRandom.rootAccountEntity().save()

        aRandom.memberEntity()
                .organizationId(organizationId)
                .rootAccountId(account.id)
                .save()
    }
}
