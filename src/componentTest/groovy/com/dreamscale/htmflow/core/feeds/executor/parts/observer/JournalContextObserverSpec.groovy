package com.dreamscale.htmflow.core.feeds.executor.parts.observer


import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FinishStatus
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window
import com.dreamscale.htmflow.core.feeds.story.feature.context.Context
import com.dreamscale.htmflow.core.feeds.story.TileBuilder
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ChangeContext
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class JournalContextObserverSpec extends Specification {

    JournalContextObserver journalContextObserver
    TileBuilder storyTile
    GeometryClock clock

    def setup() {
        clock = new GeometryClock(LocalDateTime.now())
        journalContextObserver = new JournalContextObserver()
        storyTile = new TileBuilder("@torchie/id", clock.getCoordinates(), ZoomLevel.TWENTIES)
    }

    def "should create project & task switch events"() {
        given:
        ProjectEntity project = aRandom.projectEntity().build();
        TaskEntity task1 = aRandom.taskEntity().forProject(project).build();
        TaskEntity task2 = aRandom.taskEntity().forProject(project).build();

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time2)
        FlowableJournalEntry journalEntry2 = createJournalEvent(project, task2, time2, time3)
        FlowableJournalEntry journalEntry3 = createJournalEvent(project, task2, time3, time4)

        def flowables = [journalEntry1, journalEntry2, journalEntry3] as List
        Window window = new Window(time1, time4)
        window.addAll(flowables);

        when:
        journalContextObserver.seeInto(window, storyTile)
        List<ChangeContext> contextEvents = storyTile.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES).getMovements();

        then:
        assert contextEvents != null
        assert contextEvents.size() == 9
        assert contextEvents.get(0).getStructureLevel() == StructureLevel.PROJECT
        assert contextEvents.get(0).getObjectId() == project.id
        assert contextEvents.get(0).getMoment() == time1
        assert contextEvents.get(0).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING

        assert contextEvents.get(1).getStructureLevel() == StructureLevel.TASK
        assert contextEvents.get(1).getObjectId() == task1.id
        assert contextEvents.get(1).getMoment() == time1
        assert contextEvents.get(1).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING

        assert contextEvents.get(2).getStructureLevel() == StructureLevel.INTENTION
        assert contextEvents.get(2).getObjectId() == journalEntry1.get().id
        assert contextEvents.get(2).getMoment() == time1
        assert contextEvents.get(2).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING

        assert contextEvents.get(3).getStructureLevel() == StructureLevel.INTENTION
        assert contextEvents.get(3).getObjectId() == journalEntry1.get().id
        assert contextEvents.get(3).getMoment() == time2
        assert contextEvents.get(3).getEventType() == ChangeContext.EventType.CONTEXT_ENDING

        assert contextEvents.get(4).getStructureLevel() == StructureLevel.TASK
        assert contextEvents.get(4).getObjectId() == task1.id
        assert contextEvents.get(4).getMoment() == time2
        assert contextEvents.get(4).getEventType() == ChangeContext.EventType.CONTEXT_ENDING

        assert contextEvents.get(5).getStructureLevel() == StructureLevel.TASK
        assert contextEvents.get(5).getObjectId() == task2.id
        assert contextEvents.get(5).getMoment() == time2
        assert contextEvents.get(5).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING

        assert contextEvents.get(6).getStructureLevel() == StructureLevel.INTENTION
        assert contextEvents.get(6).getObjectId() == journalEntry2.get().id
        assert contextEvents.get(6).getMoment() == time2
        assert contextEvents.get(6).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING

        assert contextEvents.get(7).getStructureLevel() == StructureLevel.INTENTION
        assert contextEvents.get(7).getObjectId() == journalEntry2.get().id
        assert contextEvents.get(7).getMoment() == time3
        assert contextEvents.get(7).getEventType() == ChangeContext.EventType.CONTEXT_ENDING

        assert contextEvents.get(8).getStructureLevel() == StructureLevel.INTENTION
        assert contextEvents.get(8).getObjectId() == journalEntry3.get().id
        assert contextEvents.get(8).getMoment() == time3
        assert contextEvents.get(8).getEventType() == ChangeContext.EventType.CONTEXT_BEGINNING
    }


    def "should only end open intentions within window"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        clock = new GeometryClock(time1);
        storyTile = new TileBuilder("@torchie/id", clock.getCoordinates(), ZoomLevel.TWENTIES);

        ProjectEntity project = aRandom.projectEntity().build();
        TaskEntity task1 = aRandom.taskEntity().forProject(project).build();


        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time3)
        journalEntry1.get().setFinishStatus(FinishStatus.aborted.name())

        def flowables = [journalEntry1] as List
        Window window = new Window(time1, time2)
        window.addAll(flowables);

        journalContextObserver.seeInto(window, storyTile)

        TileBuilder nextFrame = new TileBuilder("@torchie/id", clock.getCoordinates().panRight(ZoomLevel.TWENTIES).panRight(ZoomLevel.TWENTIES), ZoomLevel.TWENTIES);
        nextFrame.carryOverTileContext(storyTile.getCarryOverContext());
        Window nextWindow = new Window(time3, time4)

        when:
        journalContextObserver.seeInto(nextWindow, nextFrame)
        List<Movement> contextEvents = nextFrame.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES).getMovements();
        then:
        assert contextEvents != null
        assert contextEvents.size() == 1

        Context context = ((ChangeContext)contextEvents.get(0)).getChangingContext();

        assert context.structureLevel == StructureLevel.INTENTION
        assert context.objectId == journalEntry1.get().id
        assert contextEvents.get(0).moment == time3

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
