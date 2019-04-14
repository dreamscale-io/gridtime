package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of songs,
 * at two levels of story abstraction
 */
public class JournalContextObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        MomentOfContext momentOfContext = currentStoryTile.getCurrentContext();

        Context lastOpenProject = momentOfContext.getProjectContext();
        Context lastOpenTask = momentOfContext.getTaskContext();
        Context lastOpenIntention = momentOfContext.getIntentionContext();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                createIntentionDoneIfNotNull(currentStoryTile, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(currentStoryTile, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(currentStoryTile, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(currentStoryTile, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(currentStoryTile, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(window, currentStoryTile, journalEntry);

                momentOfContext = currentStoryTile.getCurrentContext();

                lastOpenProject = momentOfContext.getProjectContext();
                lastOpenTask = momentOfContext.getTaskContext();
                lastOpenIntention = momentOfContext.getIntentionContext();
            }
        }

    }

    private void createTaskStartIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            ContextBeginningEvent taskBeginning = createTaskBeginning(journalEntry);
            currentStoryTile.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            ContextBeginningEvent projectBeginning = createProjectBeginning(journalEntry);
            currentStoryTile.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            ContextEndingEvent projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryTile.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            ContextEndingEvent taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryTile.endContext(taskEnding);
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

    private ContextEndingEvent createTaskEnding(JournalEntryEntity journalEntry, Context lastTaskStart) {
        ContextEndingEvent taskEnding = new ContextEndingEvent();
        taskEnding.setReferenceId(lastTaskStart.getId());
        taskEnding.setStructureLevel(StructureLevel.TASK);
        taskEnding.setName(lastTaskStart.getName());
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);

        return taskEnding;
    }

    private ContextEndingEvent createProjectEnding(JournalEntryEntity journalEntry, Context lastOpenProject) {
        ContextEndingEvent projectEnding = new ContextEndingEvent();
        projectEnding.setReferenceId(lastOpenProject.getId());
        projectEnding.setStructureLevel(StructureLevel.PROJECT);
        projectEnding.setName(lastOpenProject.getName());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryTile storyTile, JournalEntryEntity journalEntry) {

        ContextBeginningEvent intentionStart = new ContextBeginningEvent();
        intentionStart.setReferenceId(journalEntry.getId());
        intentionStart.setStructureLevel(StructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        storyTile.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            ContextEndingEvent intentionEnd = new ContextEndingEvent();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(decodeFinishStatus(journalEntry.getFinishStatus()));
            intentionEnd.setDescription(journalEntry.getDescription());
            intentionEnd.setPosition(journalEntry.getFinishTime());

            if (window.isWithin(journalEntry.getFinishTime())) {
                storyTile.endContext(intentionEnd);
            } else {
                storyTile.endContextLater(intentionEnd);
            }

        }
    }

    private void createIntentionDoneIfNotNull(StoryTile storyTile, JournalEntryEntity journalEntry, Context lastIntentionStart) {
        ContextEndingEvent intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new ContextEndingEvent();
            intentionEnd.setReferenceId(lastIntentionStart.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(ContextEndingEvent.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            storyTile.endContext(intentionEnd);
        }

    }

    private ContextEndingEvent.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        ContextEndingEvent.FinishStatus contextFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    contextFinishStatus = ContextEndingEvent.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    contextFinishStatus = ContextEndingEvent.FinishStatus.ABORT;
                    break;
            }
        }

        return contextFinishStatus;
    }
}
