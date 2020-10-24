package com.dreamscale.gridtime.core.machine.executor.program.parts.observer

import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity
import com.dreamscale.gridtime.core.domain.journal.TaskEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityEntity
import com.dreamscale.gridtime.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.gridtime.core.domain.flow.FlowActivityType
import com.dreamscale.gridtime.api.grid.GridTableResults
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableFlowActivity
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry
import com.dreamscale.gridtime.core.machine.clock.GeometryClock
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.gridtime.core.CoreARandom.aRandom

public class ComponentSpaceObserverSpec extends Specification {

    ComponentSpaceObserver componentSpaceObserver
    GeometryClock clock
    UUID torchieId
    GridTile gridTile

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        componentSpaceObserver = new ComponentSpaceObserver()
        torchieId = UUID.randomUUID()


        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), new FeatureCache(), new BoxResolver());
    }

    def "should create Location traversals"() {
        given:

        LocalDateTime time1 = clock.getActiveGridTime().getClockTime();
        LocalDateTime time2 = time1.plusMinutes(2);

        FlowableFlowActivity activity1 = createActivity("locationA", time1, time2)
        FlowableFlowActivity activity2 = createActivity("locationB", time2, time2.plusSeconds(3))
        FlowableFlowActivity activity3 = createActivity("locationC", time2, time2.plusSeconds(10))
        FlowableFlowActivity activity4 = createActivity("locationB", time2, time2.plusSeconds(15))

        def flowables = [activity1, activity2, activity3, activity4] as List
        Window window = new Window(time1, time1.plusMinutes(20))
        window.addAll(flowables);

        when:
        componentSpaceObserver.see(window, gridTile)
        gridTile.finishAfterLoad()

        GridTableResults tileOutput = gridTile.playTrack(TrackSetKey.Navigations)
        print tileOutput

        then:
        assert tileOutput.getCell("@nav/rhythm", "20.3") == "bcb"
    }

    FlowableFlowActivity createActivity( String location, LocalDateTime start, LocalDateTime end) {

        FlowActivityEntity flowActivityEntity = aRandom.flowActivityEntity()
                .activityType(FlowActivityType.Editor)
                .start(start)
                .end(end)
                .build();

        flowActivityEntity.setMetadataField(FlowActivityMetadataField.filePath, location)

        return new FlowableFlowActivity(flowActivityEntity);

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
