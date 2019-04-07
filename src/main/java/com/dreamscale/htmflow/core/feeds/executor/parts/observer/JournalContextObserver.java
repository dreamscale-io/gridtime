package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginning;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextEnding;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of songs,
 * at two levels of story abstraction
 */
public class JournalContextObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        ContextSummary contextSummary = currentStoryTile.getCurrentContext();

        ContextBeginning lastOpenProject = contextSummary.getProjectContext();
        ContextBeginning lastOpenTask = contextSummary.getTaskContext();
        ContextBeginning lastOpenIntention = contextSummary.getIntentionContext();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                createIntentionDoneIfNotNull(currentStoryTile, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(currentStoryTile, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(currentStoryTile, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(currentStoryTile, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(currentStoryTile, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(window, currentStoryTile, journalEntry);

                contextSummary = currentStoryTile.getCurrentContext();

                lastOpenProject = contextSummary.getProjectContext();
                lastOpenTask = contextSummary.getTaskContext();
                lastOpenIntention = contextSummary.getIntentionContext();
            }
        }

    }

    private void createTaskStartIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, ContextBeginning lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextBeginning taskBeginning = createTaskBeginning(journalEntry);
            currentStoryTile.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, ContextBeginning lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextBeginning projectBeginning = createProjectBeginning(journalEntry);
            currentStoryTile.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, ContextBeginning lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getReferenceId().equals(journalEntry.getProjectId())) {
            ContextEnding projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryTile.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, ContextBeginning lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getReferenceId().equals(journalEntry.getTaskId())) {
            ContextEnding taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryTile.endContext(taskEnding);
        }
    }

    private ContextBeginning createProjectBeginning(JournalEntryEntity journalEntry) {
        ContextBeginning projectBeginning = new ContextBeginning();
        projectBeginning.setReferenceId(journalEntry.getProjectId());
        projectBeginning.setStructureLevel(ContextStructureLevel.PROJECT);
        projectBeginning.setName(journalEntry.getProjectName());
        projectBeginning.setPosition(journalEntry.getPosition());

        return projectBeginning;
    }

    private ContextBeginning createTaskBeginning(JournalEntryEntity journalEntry) {
        ContextBeginning taskBeginning = new ContextBeginning();
        taskBeginning.setReferenceId(journalEntry.getTaskId());
        taskBeginning.setStructureLevel(ContextStructureLevel.TASK);
        taskBeginning.setName(journalEntry.getTaskName());
        taskBeginning.setDescription(journalEntry.getTaskSummary());
        taskBeginning.setPosition(journalEntry.getPosition());

        return taskBeginning;
    }

    private ContextEnding createTaskEnding(JournalEntryEntity journalEntry, ContextBeginning lastTaskStart) {
        ContextEnding taskEnding = new ContextEnding();
        taskEnding.setReferenceId(lastTaskStart.getReferenceId());
        taskEnding.setStructureLevel(ContextStructureLevel.TASK);
        taskEnding.setName(lastTaskStart.getName());
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(ContextEnding.FinishStatus.SUCCESS);

        return taskEnding;
    }

    private ContextEnding createProjectEnding(JournalEntryEntity journalEntry, ContextBeginning lastOpenProject) {
        ContextEnding projectEnding = new ContextEnding();
        projectEnding.setReferenceId(lastOpenProject.getReferenceId());
        projectEnding.setStructureLevel(ContextStructureLevel.PROJECT);
        projectEnding.setName(lastOpenProject.getName());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(ContextEnding.FinishStatus.SUCCESS);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryTile storyTile, JournalEntryEntity journalEntry) {

        ContextBeginning intentionStart = new ContextBeginning();
        intentionStart.setReferenceId(journalEntry.getId());
        intentionStart.setStructureLevel(ContextStructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        storyTile.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            ContextEnding intentionEnd = new ContextEnding();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(ContextStructureLevel.INTENTION);
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

    private void createIntentionDoneIfNotNull(StoryTile storyTile, JournalEntryEntity journalEntry, ContextBeginning lastIntentionStart) {
        ContextEnding intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new ContextEnding();
            intentionEnd.setReferenceId(lastIntentionStart.getReferenceId());
            intentionEnd.setStructureLevel(ContextStructureLevel.INTENTION);
            intentionEnd.setFinishStatus(ContextEnding.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            storyTile.endContext(intentionEnd);
        }

    }

    private ContextEnding.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        ContextEnding.FinishStatus songFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    songFinishStatus = ContextEnding.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    songFinishStatus = ContextEnding.FinishStatus.ABORT;
                    break;
            }
        }

        return songFinishStatus;
    }
}
