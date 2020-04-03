package com.dreamscale.gridtime.core.machine.executor.program.parts.locas

import com.dreamscale.gridtime.ComponentTest

import com.dreamscale.gridtime.api.team.TeamDto
import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.flow.FinishStatus
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository
import com.dreamscale.gridtime.core.domain.tile.metrics.GridIdeaFlowMetricsRepository
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.GridTimeWorkPile
import com.dreamscale.gridtime.core.machine.Torchie
import com.dreamscale.gridtime.core.machine.TorchieFactory
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results
import com.dreamscale.gridtime.core.machine.clock.Metronome
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability
import com.dreamscale.gridtime.core.service.GridClock
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

@ComponentTest
class ZoomableTeamBoxLocasSpec extends Specification {

    @Autowired
    GridIdeaFlowMetricsRepository gridMetricsIdeaFlowRepository;

    @Autowired
    CalendarService calendarService;

    @Autowired
    TeamMembershipCapability teamService

    @Autowired
    OrganizationRepository organizationRepository

    @Autowired
    OrganizationMemberRepository organizationMemberRepository

    @Autowired
    TorchieFactory torchieFactory

    @Autowired
    GridClock mockTimeService

    @Autowired
    ProgramFactory programFactory

    @Autowired
    AggregateWorkToDoQueueWire workToDoQueueWire

    @Autowired
    FeatureCacheManager featureCacheManager

    @Autowired
    GridTimeWorkPile gridTimeWorkerPool

    LocalDateTime clockStart
    LocalDateTime wtfTime
    LocalDateTime time1
    LocalDateTime time2
    LocalDateTime time3
    LocalDateTime time4

    Metronome metronome

    OrganizationEntity org
    OrganizationMemberEntity member1
    OrganizationMemberEntity member2
    OrganizationMemberEntity member3

    TeamDto team

    UUID projectId

    def setup() {

        org = aRandom.organizationEntity().save()
        member1 = createMembership(org.getId())
        member2 = createMembership(org.getId())
        member3 = createMembership(org.getId())

        projectId = UUID.randomUUID()

        team = teamService.createTeam(org.getId(), member1.getId(), "Team23")

        teamService.addMemberToTeamWithMemberId(org.getId(), member1.getId(), "Team23", member1.getId())
        teamService.addMemberToTeamWithMemberId(org.getId(), member1.getId(), "Team23", member2.getId())
        teamService.addMemberToTeamWithMemberId(org.getId(), member1.getId(), "Team23", member3.getId())

        clockStart = LocalDateTime.of(2019, 1, 7, 4, 00)
        time1 = clockStart.plusMinutes(1)
        time2 = clockStart.plusMinutes(4)
        time3 = clockStart.plusMinutes(6)
        time4 = clockStart.plusMinutes(7)

        metronome = new Metronome(clockStart)

        wtfTime = clockStart.plusMinutes(1)

        Metronome.TickScope tick = metronome.tick();

        calendarService.saveCalendar(1, 12, tick.from);
        calendarService.saveCalendar(1, tick.from.zoomOut());

        mockTimeService.now() >> LocalDateTime.now()

    }


    def "should aggregate BoxMetrics by GridTime across Team23"() {

        given:

        gridTimeWorkerPool.clear()

        Torchie torchie1 = torchieFactory.wireUpMemberTorchie(team.id, member1.getId(), clockStart);
        Torchie torchie2 = torchieFactory.wireUpMemberTorchie(team.id, member2.getId(), clockStart);
        Torchie torchie3 = torchieFactory.wireUpMemberTorchie(team.id, member3.getId(), clockStart);

        gridTimeWorkerPool.submitJob(torchie1);
        gridTimeWorkerPool.submitJob(torchie2);
        gridTimeWorkerPool.submitJob(torchie3);

        TeamBoxConfiguration.Builder boxConfigBuilder = new TeamBoxConfiguration.Builder()
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("componentA", "/box1/*"))
        boxConfigBuilder.boxMatcher(projectId, new BoxMatcherConfig("componentB", "/box2/*"))

        torchie1.changeBoxConfiguration(boxConfigBuilder.build())
        torchie2.changeBoxConfiguration(boxConfigBuilder.build())
        torchie3.changeBoxConfiguration(boxConfigBuilder.build())

        addFileActivity(torchie1)
        addFileActivity(torchie2)
        addFileActivity(torchie3)

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

        TickInstructions teamInstruction = gridTimeWorkerPool.whatsNext();

        println "RUNNING LAST: "+ teamInstruction.getCmdDescription()
        teamInstruction.call()


        then:
        List<Results> results = teamInstruction.getAllOutputResults()
        for (Results result : results) {
            println result
        }

        assert results != null
        assert results.get(0).getCell("@zoom/wtf", "Calc[Avg]") == "0.68"

    }

    private void addFileActivity(Torchie torchie) {
        InputFeed journalFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.JOURNAL_FEED)
        journalFeed.addSomeData(generateIntentionStart(torchie.getTorchieId(), time1, time1.plusMinutes(34), "taskA", -3))
        journalFeed.addSomeData(generateIntentionStart(torchie.getTorchieId(), time1.plusMinutes(34), null, "taskB", 2))

        InputFeed fileActivityFeed = torchie.getInputFeed(FeedStrategyFactory.FeedType.FILE_ACTIVITY_FEED)
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time1, "/box1/file.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time2, "/box1/file2.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time3, "/box2/file3.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time1.plusMinutes(32), "/box1/file.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time1.plusMinutes(67), "/box2/fileZ.txt"))
        fileActivityFeed.addSomeData(generateFileActivity(torchie.getTorchieId(), time1.plusMinutes(140), "/box2/file4.txt"))

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
    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, String filePath) {
        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .memberId(memberId)
                .start(start)
                .end(start.plusMinutes(3))
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, filePath)

        return new FlowableFlowActivity(flowActivityEntity);

    }

    FlowableFlowActivity generateFileActivity(UUID memberId, LocalDateTime start, LocalDateTime end) {
            FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                    .memberId(memberId)
                    .start(start)
                    .end(end)
        .build();

        return new FlowableFlowActivity(flowActivityEntity);

    }

    def generateIntentionStart(UUID torchieId, LocalDateTime startTime, LocalDateTime finishTime, String taskName, int flame) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity()
        journalEntryEntity.setId(UUID.randomUUID())
        journalEntryEntity.setProjectId(projectId)
        journalEntryEntity.setProjectName("projA")
        journalEntryEntity.setPosition(startTime)
        journalEntryEntity.setTaskId(UUID.randomUUID())
        journalEntryEntity.setTaskName(taskName)
        journalEntryEntity.setFlameRating(flame)
        journalEntryEntity.setMemberId(torchieId)
        journalEntryEntity.setFinishTime(finishTime)
        if (finishTime != null) {
            journalEntryEntity.setFinishStatus(FinishStatus.done.toString());
        }

        return new FlowableJournalEntry(journalEntryEntity)

    }

    def generateWTFStart(LocalDateTime startTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.WTF_STARTED)
        wtfMessage.setPosition(startTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }

    def generateWTFEnd(LocalDateTime endTime) {
        WTFFeedMessageEntity wtfMessage = new WTFFeedMessageEntity()
        wtfMessage.setMessageType(CircuitMessageType.WTF_SOLVED)
        wtfMessage.setPosition(endTime)
        wtfMessage.setCircuitId(UUID.randomUUID())

        return new FlowableCircuitWTFMessageEvent(wtfMessage)
    }
}
