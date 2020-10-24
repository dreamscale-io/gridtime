package com.dreamscale.gridtime.core.machine.executor.program.parts.observer

import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity
import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableCircuitWTFMessageEvent
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver
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

        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), new FeatureCache(), new BoxResolver());
    }

    def "should create wtf circuit states"() {
        given:

        LocalDateTime time1 = clock.getActiveGridTime().getClockTime();
        LocalDateTime time2 = time1.plusMinutes(2);
        LocalDateTime time3 = time1.plusMinutes(4);
        LocalDateTime time4 = time1.plusMinutes(11);
        LocalDateTime time5 = time1.plusMinutes(15);

        UUID circuitId = UUID.randomUUID()

        FlowableCircuitWTFMessageEvent circuitEvent1 = createCircuitMessageEvent(time2, circuitId, "circle", CircuitMessageType.TEAM_WTF_STARTED)
        FlowableCircuitWTFMessageEvent circuitEvent2 = createCircuitMessageEvent(time3, circuitId, "circle", CircuitMessageType.TEAM_WTF_ON_HOLD)
        FlowableCircuitWTFMessageEvent circuitEvent3 = createCircuitMessageEvent(time4, circuitId, "circle", CircuitMessageType.TEAM_WTF_RESUMED)
        FlowableCircuitWTFMessageEvent circuitEvent4 = createCircuitMessageEvent(time5, circuitId, "circle", CircuitMessageType.TEAM_WTF_SOLVED)


        def flowables = [circuitEvent1, circuitEvent2, circuitEvent3, circuitEvent4] as List
        Window window = new Window(time1, time1.plusMinutes(20))
        window.addAll(flowables);

        when:
        wtfStateObserver.see(window, gridTile)
        gridTile.finishAfterLoad()

        GridTableResults tileOutput = gridTile.playTrack(TrackSetKey.IdeaFlow)
        print tileOutput

        then:
        assert tileOutput.getCell("@flow/wtf", "20.3") == "wtf^"
        assert tileOutput.getCell("@flow/wtf", "20.5") == "wtf\$"
        assert tileOutput.getCell("@flow/wtf", "20.6") == ""
        assert tileOutput.getCell("@flow/wtf", "20.12") == "wtf^"
        assert tileOutput.getCell("@flow/wtf", "20.16") == "wtf\$"
        assert tileOutput.getCell("@flow/wtf", "20.17") == ""

    }


    FlowableCircuitWTFMessageEvent createCircuitMessageEvent(LocalDateTime position, UUID circuitId, String circuitName, CircuitMessageType messageType) {

        WTFFeedMessageEntity circuitMessage = new WTFFeedMessageEntity()
        circuitMessage.setId(UUID.randomUUID())
        circuitMessage.setCircuitId(circuitId)
        circuitMessage.setCircuitName(circuitName)
        circuitMessage.setPosition(position)
        circuitMessage.setCircuitMessageType(messageType)

        return new FlowableCircuitWTFMessageEvent(circuitMessage);

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
