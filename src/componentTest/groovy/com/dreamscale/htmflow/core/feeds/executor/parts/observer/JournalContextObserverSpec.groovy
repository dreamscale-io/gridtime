package com.dreamscale.htmflow.core.feeds.executor.parts.observer


import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity
import com.dreamscale.htmflow.core.domain.journal.ProjectEntity
import com.dreamscale.htmflow.core.domain.journal.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FinishStatus
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window

import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent
import com.dreamscale.htmflow.core.feeds.story.StoryFrame
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ChangeContext
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class JournalContextObserverSpec extends Specification {

    JournalContextObserver journalContextObserver
    StoryFrame storyFrame
    GeometryClock clock

    def setup() {
        clock = new GeometryClock(LocalDateTime.now())
        journalContextObserver = new JournalContextObserver()
        storyFrame = new StoryFrame("@torchie/id", clock.getCoordinates(), ZoomLevel.MIN)
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
        journalContextObserver.see(storyFrame, window)
        List<ContextChangeEvent> contextEvents = toContextEventList(storyFrame.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES).getMovements());

        then:
        assert contextEvents != null
        assert contextEvents.size() == 9
        assert contextEvents.get(0).structureLevel == ContextStructureLevel.PROJECT
        assert contextEvents.get(0).referenceId == project.id
        assert contextEvents.get(0).position == time1
        assert contextEvents.get(0).eventType == ContextChangeEvent.Type.BEGINNING

        assert contextEvents.get(1).structureLevel == ContextStructureLevel.TASK
        assert contextEvents.get(1).referenceId == task1.id
        assert contextEvents.get(1).position == time1
        assert contextEvents.get(1).eventType == ContextChangeEvent.Type.BEGINNING

        assert contextEvents.get(2).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(2).referenceId == journalEntry1.get().id
        assert contextEvents.get(2).position == time1
        assert contextEvents.get(2).eventType == ContextChangeEvent.Type.BEGINNING

        assert contextEvents.get(3).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(3).referenceId == journalEntry1.get().id
        assert contextEvents.get(3).position == time2
        assert contextEvents.get(3).eventType == ContextChangeEvent.Type.ENDING

        assert contextEvents.get(4).structureLevel == ContextStructureLevel.TASK
        assert contextEvents.get(4).referenceId == task1.id
        assert contextEvents.get(4).position == time2
        assert contextEvents.get(4).eventType == ContextChangeEvent.Type.ENDING

        assert contextEvents.get(5).structureLevel == ContextStructureLevel.TASK
        assert contextEvents.get(5).referenceId == task2.id
        assert contextEvents.get(5).position == time2
        assert contextEvents.get(5).eventType == ContextChangeEvent.Type.BEGINNING

        assert contextEvents.get(6).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(6).referenceId == journalEntry2.get().id
        assert contextEvents.get(6).position == time2
        assert contextEvents.get(6).eventType == ContextChangeEvent.Type.BEGINNING

        assert contextEvents.get(7).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(7).referenceId == journalEntry2.get().id
        assert contextEvents.get(7).position == time3
        assert contextEvents.get(7).eventType == ContextChangeEvent.Type.ENDING

        assert contextEvents.get(8).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(8).referenceId == journalEntry3.get().id
        assert contextEvents.get(8).position == time3
        assert contextEvents.get(8).eventType == ContextChangeEvent.Type.BEGINNING
    }


    def "should only end open intentions within window"() {
        given:

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        clock = new GeometryClock(time1);
        storyFrame = new StoryFrame("@torchie/id", clock.getCoordinates(), ZoomLevel.MIN);

        ProjectEntity project = aRandom.projectEntity().build();
        TaskEntity task1 = aRandom.taskEntity().forProject(project).build();


        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time3)
        journalEntry1.get().setFinishStatus(FinishStatus.aborted.name())

        def flowables = [journalEntry1] as List
        Window window = new Window(time1, time2)
        window.addAll(flowables);

        journalContextObserver.see(storyFrame, window)

        StoryFrame nextFrame = new StoryFrame("@torchie/id", clock.getCoordinates().panRight(ZoomLevel.MIN).panRight(ZoomLevel.MIN), ZoomLevel.MIN);
        nextFrame.carryOverFrameContext(storyFrame);
        Window nextWindow = new Window(time3, time4)

        when:
        journalContextObserver.see(nextFrame, nextWindow)
        List<ContextChangeEvent> contextEvents = toContextEventList(nextFrame.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES).getMovements());
        then:
        assert contextEvents != null
        assert contextEvents.size() == 1
        assert contextEvents.get(0).structureLevel == ContextStructureLevel.INTENTION
        assert contextEvents.get(0).referenceId == journalEntry1.get().id
        assert contextEvents.get(0).position == time3
        assert contextEvents.get(0).eventType == ContextChangeEvent.Type.ENDING

    }

    private List<ContextChangeEvent> toContextEventList(List<Movement> movements) {

        List<ContextChangeEvent> contextEvents = new ArrayList<>();
        for (Movement movementEvent : movements) {
            contextEvents.add(((ChangeContext)movementEvent).getEvent() as ContextChangeEvent);
        }
        contextEvents
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
