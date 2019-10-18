package com.dreamscale.gridtime.core.machine.executor.program.parts.observer

import com.dreamscale.gridtime.api.circle.CircleMessageType
import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageEntity
import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircleMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

public class WTFStateObserverSpec extends Specification {

    WTFStateObserver wtfStateObserver
    GeometryClock clock
    UUID torchieId
    GridTile gridTile

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        wtfStateObserver = new WTFStateObserver()
        torchieId = UUID.randomUUID()

        TeamBoxConfiguration teamBoxConfiguration = new TeamBoxConfiguration.Builder().build();

        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), new FeatureCache(), teamBoxConfiguration);
    }

    def "should create wtf circle states"() {
        given:

        LocalDateTime time1 = clock.getActiveGridTime().getClockTime();
        LocalDateTime time2 = time1.plusMinutes(2);
        LocalDateTime time3 = time1.plusMinutes(4);
        LocalDateTime time4 = time1.plusMinutes(11);
        LocalDateTime time5 = time1.plusMinutes(15);

        UUID circleId = UUID.randomUUID()

        FlowableCircleMessageEvent circleEvent1 = createCircleEvent(time2, circleId, "circle", CircleMessageType.CIRCLE_START)
        FlowableCircleMessageEvent circleEvent2 = createCircleEvent(time3, circleId, "circle", CircleMessageType.CIRCLE_SHELVED)
        FlowableCircleMessageEvent circleEvent3 = createCircleEvent(time4, circleId, "circle", CircleMessageType.CIRCLE_RESUMED)
        FlowableCircleMessageEvent circleEvent4 = createCircleEvent(time5, circleId, "circle", CircleMessageType.CIRCLE_CLOSED)


        def flowables = [circleEvent1, circleEvent2, circleEvent3, circleEvent4] as List
        Window window = new Window(time1, time1.plusMinutes(20))
        window.addAll(flowables);

        when:
        wtfStateObserver.see(window, gridTile)
        gridTile.finishAfterLoad()

        MusicGridResults tileOutput = gridTile.playTrack(TrackSetKey.IdeaFlow)
        print tileOutput

        then:
        assert tileOutput.getCell("@flow/wtf", "20.3") == "wtf^"
        assert tileOutput.getCell("@flow/wtf", "20.5") == "wtf\$"
        assert tileOutput.getCell("@flow/wtf", "20.6") == ""
        assert tileOutput.getCell("@flow/wtf", "20.12") == "wtf^"
        assert tileOutput.getCell("@flow/wtf", "20.16") == "wtf\$"
        assert tileOutput.getCell("@flow/wtf", "20.17") == ""

    }


    FlowableCircleMessageEvent createCircleEvent(LocalDateTime position, UUID circleId, String circleName, CircleMessageType messageType) {

        CircleFeedMessageEntity circleMessage = new CircleFeedMessageEntity()
        circleMessage.setId(UUID.randomUUID())
        circleMessage.setCircleId(circleId)
        circleMessage.setCircleName(circleName)
        circleMessage.setPosition(position)
        circleMessage.setMessageType(messageType)

        return new FlowableCircleMessageEvent(circleMessage);

    }


    FlowableJournalEntry createJournalEvent(ProjectEntity project, TaskEntity task, LocalDateTime time,
                                            LocalDateTime finishTime) {
        JournalEntryEntity journalEntryEntity = new JournalEntryEntity()

        journalEntryEntity.id = UUID.randomUUID()
        journalEntryEntity.projectId = project.id
        journalEntryEntity.taskId = task.id
        journalEntryEntity.position = time
        journalEntryEntity.finishTime = finishTime
        journalEntryEntity.description = aRandom.text(20)
        journalEntryEntity.taskName = aRandom.text(5)
        journalEntryEntity.taskSummary = aRandom.text(30)

        return new FlowableJournalEntry(journalEntryEntity)
    }
}
