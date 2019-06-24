package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityEntity
import com.dreamscale.htmflow.core.domain.flow.FlowActivityMetadataField
import com.dreamscale.htmflow.core.domain.flow.FlowActivityType
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.MusicGridResults
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableFlowActivity
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Window
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeatureCache
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class ComponentSpaceObserverSpec extends Specification {

    ComponentSpaceObserver componentSpaceObserver
    GridTile gridTile
    GeometryClock clock
    UUID torchieId
    FeatureCache featureCache

    def setup() {

        clock = new GeometryClock(LocalDateTime.now())
        componentSpaceObserver = new ComponentSpaceObserver()
        torchieId = UUID.randomUUID()
        featureCache = new FeatureCache()
        gridTile = new GridTile(torchieId, clock.getActiveGridTime(), featureCache)
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

        MusicGridResults tileOutput = gridTile.getMusicGrid().playTrackSet(TrackSetName.Navigations)
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
