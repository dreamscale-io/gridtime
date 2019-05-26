package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.feeds.story.feature.context.*;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of musical sequences
 */
public class JournalContextObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void seeInto(List<FlowableJournalEntry> flowables, TileBuilder tileBuilder) {


        MomentOfContext momentOfContext = tileBuilder.getCurrentContext();

        Context lastOpenProject = momentOfContext.getProjectContext();
        Context lastOpenTask = momentOfContext.getTaskContext();
        Context lastOpenIntention = momentOfContext.getIntentionContext();

        for (FlowableJournalEntry flowable : flowables) {
                JournalEntryEntity journalEntry = flowable.get();

                createIntentionDoneIfNotNull(tileBuilder, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(tileBuilder, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(tileBuilder, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(tileBuilder, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(tileBuilder, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(tileBuilder, journalEntry);

                momentOfContext = tileBuilder.getCurrentContext();

                lastOpenProject = momentOfContext.getProjectContext();
                lastOpenTask = momentOfContext.getTaskContext();
                lastOpenIntention = momentOfContext.getIntentionContext();

        }

    }

    private void createTaskStartIfSwitched(TileBuilder currentTileBuilder, JournalEntryEntity journalEntry, Context lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            MusicalSequenceBeginning taskBeginning = createTaskBeginning(journalEntry);
            currentTileBuilder.beginContext(taskBeginning);
        }
    }

    private void createProjectStartIfSwitched(TileBuilder currentTileBuilder, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            MusicalSequenceBeginning projectBeginning = createProjectBeginning(journalEntry);
            currentTileBuilder.beginContext(projectBeginning);
        }
    }

    private void createProjectDoneIfSwitched(TileBuilder currentTileBuilder, JournalEntryEntity journalEntry, Context lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getId().equals(journalEntry.getProjectId())) {
            MusicalSequenceEnding projectEnding = createProjectEnding(journalEntry, lastOpenProject);
            currentTileBuilder.endContext(projectEnding);
        }
    }

    private void createTaskDoneIfSwitched(TileBuilder currentTileBuilder, JournalEntryEntity journalEntry, Context lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            MusicalSequenceEnding taskEnding = createTaskEnding(journalEntry, lastOpenTask);
            currentTileBuilder.endContext(taskEnding);
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


    private void createIntentionStartAndEnd(TileBuilder tileBuilder, JournalEntryEntity journalEntry) {

        MusicalSequenceBeginning intentionStart = new MusicalSequenceBeginning();
        intentionStart.setContextId(journalEntry.getId());
        intentionStart.setStructureLevel(StructureLevel.INTENTION);
        intentionStart.setDescription(journalEntry.getDescription());
        intentionStart.setPosition(journalEntry.getPosition());

        tileBuilder.beginContext(intentionStart);

        if (journalEntry.getFinishTime() != null) {
            MusicalSequenceEnding intentionEnd = new MusicalSequenceEnding();
            intentionEnd.setReferenceId(journalEntry.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(decodeFinishStatus(journalEntry.getFinishStatus()));
            intentionEnd.setDescription(journalEntry.getDescription());
            intentionEnd.setPosition(journalEntry.getFinishTime());

            tileBuilder.endContext(intentionEnd);

//            if (storyTile.isWithin(journalEntry.getFinishTime())) {
//                storyTile.endContext(intentionEnd);
//            } else {
//                storyTile.endContextLater(intentionEnd);
//            }

        }
    }

    private void createIntentionDoneIfNotNull(TileBuilder tileBuilder, JournalEntryEntity journalEntry, Context lastIntentionStart) {
        MusicalSequenceEnding intentionEnd = null;

        if (lastIntentionStart != null) {
            intentionEnd = new MusicalSequenceEnding();
            intentionEnd.setReferenceId(lastIntentionStart.getId());
            intentionEnd.setStructureLevel(StructureLevel.INTENTION);
            intentionEnd.setFinishStatus(MusicalSequenceEnding.FinishStatus.SUCCESS);
            intentionEnd.setDescription(lastIntentionStart.getDescription());
            intentionEnd.setPosition(journalEntry.getPosition());

            tileBuilder.endContext(intentionEnd);
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
