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
    public void see(Window window, StoryTile currentStoryTile) {

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
            MusicalSequenceBeginning taskBeginning = createTaskBeginning(journalEntry);
            currentStoryTile.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            MusicalSequenceBeginning projectBeginning = createProjectBeginning(journalEntry);
            currentStoryTile.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            MusicalSequenceEnding projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentStoryTile.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(StoryTile currentStoryTile, JournalEntryEntity journalEntry, Context lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            MusicalSequenceEnding taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentStoryTile.endContext(taskEnding);
        }
    }

    private MusicalSequenceBeginning createProjectBeginning(JournalEntryEntity journalEntry) {
        MusicalSequenceBeginning projectBeginning = new MusicalSequenceBeginning();
        projectBeginning.setContextId(journalEntry.getProjectId());
        projectBeginning.setStructureLevel(StructureLevel.PROJECT);
        projectBeginning.setDescription(journalEntry.getProjectName());
        projectBeginning.setPosition(journalEntry.getPosition());

        return projectBeginning;
    }

    private MusicalSequenceBeginning createTaskBeginning(JournalEntryEntity journalEntry) {
        MusicalSequenceBeginning taskBeginning = new MusicalSequenceBeginning();
        taskBeginning.setContextId(journalEntry.getTaskId());
        taskBeginning.setStructureLevel(StructureLevel.TASK);
        taskBeginning.setDescription(journalEntry.getTaskName());
        taskBeginning.setPosition(journalEntry.getPosition());

        return taskBeginning;
    }

    private MusicalSequenceEnding createTaskEnding(JournalEntryEntity journalEntry, Context lastTaskStart) {
        MusicalSequenceEnding taskEnding = new MusicalSequenceEnding();
        taskEnding.setReferenceId(lastTaskStart.getId());
        taskEnding.setStructureLevel(StructureLevel.TASK);
        taskEnding.setDescription(lastTaskStart.getDescription());
        taskEnding.setPosition(journalEntry.getPosition());
        taskEnding.setFinishStatus(MusicalSequenceEnding.FinishStatus.SUCCESS);

        return taskEnding;
    }

    private MusicalSequenceEnding createProjectEnding(JournalEntryEntity journalEntry, Context lastOpenProject) {
        MusicalSequenceEnding projectEnding = new MusicalSequenceEnding();
        projectEnding.setReferenceId(lastOpenProject.getId());
        projectEnding.setStructureLevel(StructureLevel.PROJECT);
        projectEnding.setDescription(lastOpenProject.getDescription());
        projectEnding.setPosition(journalEntry.getPosition());
        projectEnding.setFinishStatus(MusicalSequenceEnding.FinishStatus.SUCCESS);

        return projectEnding;
    }


    private void createIntentionStartAndEnd(Window window, StoryTile storyTile, JournalEntryEntity journalEntry) {

        MusicalSequenceBeginning intentionStart = new MusicalSequenceBeginning();
        intentionStart.setContextId(journalEntry.getId());
        intentionStart.setStructureLevel(StructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        storyTile.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            MusicalSequenceEnding intentionEnd = new MusicalSequenceEnding();
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
        MusicalSequenceEnding intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new MusicalSequenceEnding();
            intentionEnd.setReferenceId(lastIntentionStart.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(MusicalSequenceEnding.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            storyTile.endContext(intentionEnd);
        }

    }

    private MusicalSequenceEnding.FinishStatus decodeFinishStatus(String journalFinishStatus) {
        MusicalSequenceEnding.FinishStatus contextFinishStatus = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    contextFinishStatus = MusicalSequenceEnding.FinishStatus.SUCCESS;
                    break;
                case aborted:
                    contextFinishStatus = MusicalSequenceEnding.FinishStatus.ABORT;
                    break;
            }
        }

        return contextFinishStatus;
    }
}
