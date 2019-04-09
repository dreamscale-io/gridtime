package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of songs,
 * at two levels of story abstraction
 */
public class JournalContextObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        ContextSummary contextSummary = currentStoryFrame.getCurrentContext();

        ContextChangeEvent lastOpenProject = contextSummary.getProjectContext();
        ContextChangeEvent lastOpenTask = contextSummary.getTaskContext();
        ContextChangeEvent lastOpenIntention = contextSummary.getIntentionContext();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                createIntentionDoneIfNotNull(currentStoryFrame, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(currentStoryFrame, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(currentStoryFrame, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(window, currentStoryFrame, journalEntry);

                contextSummary = currentStoryFrame.getCurrentContext();

                lastOpenProject = contextSummary.getProjectContext();
                lastOpenTask = contextSummary.getTaskContext();
                lastOpenIntention = contextSummary.getIntentionContext();
            }
        }

    }

    private void createTaskStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextChangeEvent lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextChangeEvent taskBeginning = createTaskBeginning(journalEntry);
            currentStoryFrame.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextChangeEvent lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextChangeEvent projectBeginning = createProjectBeginning(journalEntry);
            currentStoryFrame.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextChangeEvent lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextChangeEvent projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryFrame.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryFrame currentStoryFrame, JournalEntryEntity journalEntry, ContextChangeEvent lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextChangeEvent taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryFrame.endContext(taskEnding);
        }
    }

    private ContextChangeEvent createProjectBeginning(JournalEntryEntity journalEntry) {
        ContextChangeEvent projectBeginning = new ContextChangeEvent();
        projectBeginning.setReferenceId(journalEntry.getProjectId());
        projectBeginning.setStructureLevel(ContextStructureLevel.PROJECT);
        projectBeginning.setName(journalEntry.getProjectName());
        projectBeginning.setPosition(journalEntry.getPosition());
        projectBeginning.setEventType(ContextChangeEvent.Type.BEGINNING);

        return projectBeginning;
    }

    private ContextChangeEvent createTaskBeginning(JournalEntryEntity journalEntry) {
        ContextChangeEvent taskBeginning = new ContextChangeEvent();
        taskBeginning.setReferenceId(journalEntry.getTaskId());
        taskBeginning.setStructureLevel(ContextStructureLevel.TASK);
        taskBeginning.setName(journalEntry.getTaskName());
        taskBeginning.setDescription(journalEntry.getTaskSummary());
        taskBeginning.setPosition(journalEntry.getPosition());
        taskBeginning.setEventType(ContextChangeEvent.Type.BEGINNING);

        return taskBeginning;
    }

    private ContextChangeEvent createTaskEnding(JournalEntryEntity journalEntry, ContextChangeEvent lastTaskStart) {
        ContextChangeEvent taskEnding = new ContextChangeEvent();
        taskEnding.setReferenceId(lastTaskStart.getReferenceId());
        taskEnding.setStructureLevel(ContextStructureLevel.TASK);
        taskEnding.setName(lastTaskStart.getName());
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(ContextChangeEvent.FinishStatus.SUCCESS);
        taskEnding.setEventType(ContextChangeEvent.Type.ENDING);

        return taskEnding;
    }

    private ContextChangeEvent createProjectEnding(JournalEntryEntity journalEntry, ContextChangeEvent lastOpenProject) {
        ContextChangeEvent projectEnding = new ContextChangeEvent();
        projectEnding.setReferenceId(lastOpenProject.getReferenceId());
        projectEnding.setStructureLevel(ContextStructureLevel.PROJECT);
        projectEnding.setName(lastOpenProject.getName());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(ContextChangeEvent.FinishStatus.SUCCESS);
        projectEnding.setEventType(ContextChangeEvent.Type.ENDING);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryFrame storyFrame, JournalEntryEntity journalEntry) {

        ContextChangeEvent intentionStart = new ContextChangeEvent();
        intentionStart.setReferenceId(journalEntry.getId());
        intentionStart.setStructureLevel(ContextStructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());
        intentionStart.setEventType(ContextChangeEvent.Type.BEGINNING);

        storyFrame.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            ContextChangeEvent intentionEnd = new ContextChangeEvent();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(ContextStructureLevel.INTENTION);
            intentionEnd.setFinishStatus(decodeFinishStatus(journalEntry.getFinishStatus()));
            intentionEnd.setDescription(journalEntry.getDescription());
            intentionEnd.setPosition(journalEntry.getFinishTime());
            intentionEnd.setEventType(ContextChangeEvent.Type.ENDING);

            if (window.isWithin(journalEntry.getFinishTime())) {
                storyFrame.endContext(intentionEnd);
            } else {
                storyFrame.endContextLater(intentionEnd);
            }

        }
    }

    private void createIntentionDoneIfNotNull(StoryFrame storyFrame, JournalEntryEntity journalEntry, ContextChangeEvent lastIntentionStart) {
        ContextChangeEvent intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new ContextChangeEvent();
            intentionEnd.setReferenceId(lastIntentionStart.getReferenceId());
            intentionEnd.setStructureLevel(ContextStructureLevel.INTENTION);
            intentionEnd.setFinishStatus(ContextChangeEvent.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());
            intentionEnd.setEventType(ContextChangeEvent.Type.ENDING);

            storyFrame.endContext(intentionEnd);
        }

    }

    private ContextChangeEvent.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        ContextChangeEvent.FinishStatus contextFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    contextFinishStatus = ContextChangeEvent.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    contextFinishStatus = ContextChangeEvent.FinishStatus.ABORT;
                    break;
            }
        }

        return contextFinishStatus;
    }
}
