package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEndingEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of songs,
 * at two levels of story abstraction
 */
public class JournalContextObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        ContextBeginningEvent lastOpenProject = currentStoryFrame.getCurrentContext(StructureLevel.PROJECT);
        ContextBeginningEvent lastOpenTask = currentStoryFrame.getCurrentContext(StructureLevel.TASK);
        ContextBeginningEvent lastOpenIntention = currentStoryFrame.getCurrentContext(StructureLevel.INTENTION);

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                createIntentionDoneIfNotNull(currentStoryFrame, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(window, currentStoryFrame, journalEntry);

                lastOpenProject = currentStoryFrame.getCurrentContext(StructureLevel.PROJECT);
                lastOpenTask = currentStoryFrame.getCurrentContext(StructureLevel.TASK);
                lastOpenIntention = currentStoryFrame.getCurrentContext(StructureLevel.INTENTION);

            }
        }

    }

    private void createTaskStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextBeginningEvent lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextBeginningEvent taskBeginning = createTaskBeginning(journalEntry);
            currentStoryFrame.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextBeginningEvent lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextBeginningEvent projectBeginning = createProjectBeginning(journalEntry);
            currentStoryFrame.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextBeginningEvent lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextEndingEvent projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryFrame.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextBeginningEvent lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextEndingEvent taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryFrame.endContext(taskEnding);
        }
    }

    private ContextBeginningEvent createProjectBeginning(JournalEntryEntity journalEntry) {
        ContextBeginningEvent projectBeginning = new ContextBeginningEvent();
        projectBeginning.setReferenceId(journalEntry.getProjectId());
        projectBeginning.setStructureLevel(StructureLevel.PROJECT);
        projectBeginning.setName(journalEntry.getProjectName());
        projectBeginning.setPosition(journalEntry.getPosition());

        return projectBeginning;
    }

    private ContextBeginningEvent createTaskBeginning(JournalEntryEntity journalEntry) {
        ContextBeginningEvent taskBeginning = new ContextBeginningEvent();
        taskBeginning.setReferenceId(journalEntry.getTaskId());
        taskBeginning.setStructureLevel(StructureLevel.TASK);
        taskBeginning.setName(journalEntry.getTaskName());
        taskBeginning.setDescription(journalEntry.getTaskSummary());
        taskBeginning.setPosition(journalEntry.getPosition());

        return taskBeginning;
    }

    private ContextEndingEvent createTaskEnding(JournalEntryEntity journalEntry, ContextBeginningEvent lastTaskStart) {
        ContextEndingEvent taskEnding = new ContextEndingEvent();
        taskEnding.setReferenceId(lastTaskStart.getReferenceId());
        taskEnding.setStructureLevel(StructureLevel.TASK);
        taskEnding.setName(lastTaskStart.getName());
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);

        return taskEnding;
    }

    private ContextEndingEvent createProjectEnding(JournalEntryEntity journalEntry, ContextBeginningEvent lastOpenProject) {
        ContextEndingEvent projectEnding = new ContextEndingEvent();
        projectEnding.setReferenceId(lastOpenProject.getReferenceId());
        projectEnding.setStructureLevel(StructureLevel.PROJECT);
        projectEnding.setName(lastOpenProject.getName());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryFrame storyFrame, JournalEntryEntity journalEntry) {

        ContextBeginningEvent intentionStart = new ContextBeginningEvent();
        intentionStart.setReferenceId(journalEntry.getId());
        intentionStart.setStructureLevel(StructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        storyFrame.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            ContextEndingEvent intentionEnd = new ContextEndingEvent();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(decodeFinishStatus(journalEntry.getFinishStatus()));
            intentionEnd.setDescription(journalEntry.getDescription());
            intentionEnd.setPosition(journalEntry.getFinishTime());

            if (window.isWithin(journalEntry.getFinishTime())) {
                storyFrame.endContext(intentionEnd);
            } else {
                storyFrame.endContextLater(intentionEnd);
            }

        }
    }

    private void createIntentionDoneIfNotNull(StoryFrame storyFrame, JournalEntryEntity journalEntry, ContextBeginningEvent lastIntentionStart) {
        ContextEndingEvent intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new ContextEndingEvent();
            intentionEnd.setReferenceId(lastIntentionStart.getReferenceId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            storyFrame.endContext(intentionEnd);
        }

    }

    private ContextEndingEvent.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        ContextEndingEvent.FinishStatus songFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    songFinishStatus = ContextEndingEvent.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    songFinishStatus = ContextEndingEvent.FinishStatus.ABORT;
                    break;
            }
        }

        return songFinishStatus;
    }
}
