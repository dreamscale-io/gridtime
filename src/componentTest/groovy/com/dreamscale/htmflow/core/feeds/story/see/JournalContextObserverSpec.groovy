package com.dreamscale.htmflow.core.feeds.story.see


import com.dreamscale.htmflow.core.domain.JournalEntryEntity
import com.dreamscale.htmflow.core.domain.ProjectEntity
import com.dreamscale.htmflow.core.domain.TaskEntity
import com.dreamscale.htmflow.core.domain.flow.FinishStatus
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent
import com.dreamscale.htmflow.core.feeds.story.StoryFrame
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent
import spock.lang.Specification

import java.time.LocalDateTime

import static com.dreamscale.htmflow.core.CoreARandom.aRandom

public class JournalContextObserverSpec extends Specification {

    JournalContextObserver journalContextObserver
    StoryFrame storyFrame
    OuterGeometryClock clock

    def setup() {
        clock = new OuterGeometryClock(LocalDateTime.now())
        journalContextObserver = new JournalContextObserver()
        storyFrame = new StoryFrame(clock.getCoordinates(), ZoomLevel.MIN)
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
        List<ContextChangeEvent> contextEvents = toContextEventList(storyFrame.getContextMovements());

        then:
        assert contextEvents != null
        assert contextEvents.size() == 9
        assert contextEvents.get(0).structureLevel == StructureLevel.PROJECT
        assert contextEvents.get(0).referenceId == project.id
        assert contextEvents.get(0).position == time1
        assert contextEvents.get(0) instanceof ContextBeginningEvent

        assert contextEvents.get(1).structureLevel == StructureLevel.TASK
        assert contextEvents.get(1).referenceId == task1.id
        assert contextEvents.get(1).position == time1
        assert contextEvents.get(1) instanceof ContextBeginningEvent

        assert contextEvents.get(2).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(2).referenceId == journalEntry1.get().id
        assert contextEvents.get(2).position == time1
        assert contextEvents.get(2) instanceof ContextBeginningEvent

        assert contextEvents.get(3).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(3).referenceId == journalEntry1.get().id
        assert contextEvents.get(3).position == time2
        assert contextEvents.get(3) instanceof ContextEndingEvent

        assert contextEvents.get(4).structureLevel == StructureLevel.TASK
        assert contextEvents.get(4).referenceId == task1.id
        assert contextEvents.get(4).position == time2
        assert contextEvents.get(4) instanceof ContextEndingEvent

        assert contextEvents.get(5).structureLevel == StructureLevel.TASK
        assert contextEvents.get(5).referenceId == task2.id
        assert contextEvents.get(5).position == time2
        assert contextEvents.get(5) instanceof ContextBeginningEvent

        assert contextEvents.get(6).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(6).referenceId == journalEntry2.get().id
        assert contextEvents.get(6).position == time2
        assert contextEvents.get(6) instanceof ContextBeginningEvent

        assert contextEvents.get(7).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(7).referenceId == journalEntry2.get().id
        assert contextEvents.get(7).position == time3
        assert contextEvents.get(7) instanceof ContextEndingEvent

        assert contextEvents.get(8).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(8).referenceId == journalEntry3.get().id
        assert contextEvents.get(8).position == time3
        assert contextEvents.get(8) instanceof ContextBeginningEvent
    }


    def "should only end open intentions within window"() {
        given:
        ProjectEntity project = aRandom.projectEntity().build();
        TaskEntity task1 = aRandom.taskEntity().forProject(project).build();

        LocalDateTime time1 = aRandom.localDateTime()
        LocalDateTime time2 = time1.plusMinutes(20);
        LocalDateTime time3 = time2.plusMinutes(20);
        LocalDateTime time4 = time3.plusMinutes(20);

        FlowableJournalEntry journalEntry1 = createJournalEvent(project, task1, time1, time3)
        journalEntry1.get().setFinishStatus(FinishStatus.aborted.name())

        def flowables = [journalEntry1] as List
        Window window = new Window(time1, time2)
        window.addAll(flowables);

        journalContextObserver.see(storyFrame, window)

        StoryFrame nextFrame = new StoryFrame(clock.getCoordinates().panRight(ZoomLevel.MIN), ZoomLevel.MIN);
        nextFrame.carryOverFrameContext(storyFrame);
        Window nextWindow = new Window(time2, time4)

        when:
        journalContextObserver.see(nextFrame, nextWindow)
        List<ContextChangeEvent> contextEvents = toContextEventList(nextFrame.getContextMovements());
        then:
        assert contextEvents != null
        assert contextEvents.size() == 1
        assert contextEvents.get(0).structureLevel == StructureLevel.INTENTION
        assert contextEvents.get(0).referenceId == journalEntry1.get().id
        assert contextEvents.get(0).position == time3
        assert contextEvents.get(0) instanceof ContextEndingEvent

    }

    private List<ContextChangeEvent> toContextEventList(List<MovementEvent> movements) {

        List<ContextChangeEvent> contextEvents = new ArrayList<>();
        for (MovementEvent movementEvent : movements) {
            contextEvents.add(movementEvent.reference as ContextChangeEvent);
            println movementEvent.reference
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
