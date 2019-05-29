package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.WorkContextReference;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.StructureLevel;

import java.util.List;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of musical sequences
 */
public class JournalContextObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void seeInto(List<FlowableJournalEntry> flowables, GridTile gridTile) {

        WorkContextReference lastOpenProject = gridTile.getFirstContext(StructureLevel.PROJECT);
        WorkContextReference lastOpenTask = gridTile.getFirstContext(StructureLevel.TASK);
        WorkContextReference lastOpenIntention = gridTile.getFirstContext(StructureLevel.INTENTION);

        for (FlowableJournalEntry flowable : flowables) {
                JournalEntryEntity journalEntry = flowable.get();

                createIntentionDoneIfNotNull(gridTile, journalEntry, lastOpenIntention);
                createTaskDoneIfSwitched(gridTile, journalEntry, lastOpenTask);
                createProjectDoneIfSwitched(gridTile, journalEntry, lastOpenProject);

                createProjectStartIfSwitched(gridTile, journalEntry, lastOpenProject);
                createTaskStartIfSwitched(gridTile, journalEntry, lastOpenTask);
                createIntentionStartAndEnd(gridTile, journalEntry);

            lastOpenProject = gridTile.getLastContext(StructureLevel.PROJECT);
            lastOpenTask = gridTile.getLastContext(StructureLevel.TASK);
            lastOpenIntention = gridTile.getLastContext(StructureLevel.INTENTION);

        }

    }

    private void createTaskStartIfSwitched(GridTile gridTile, JournalEntryEntity journalEntry, WorkContextReference lastOpenTask) {
        if (lastOpenTask == null || !lastOpenTask.getId().equals(journalEntry.getTaskId())) {

            gridTile.beginContext(
                    journalEntry.getPosition(),
                    StructureLevel.TASK,
                    journalEntry.getTaskId(),
                    journalEntry.getTaskName());
        }
    }

    private void createProjectStartIfSwitched(GridTile gridTile, JournalEntryEntity journalEntry, WorkContextReference lastOpenProject) {
        if (lastOpenProject == null || !lastOpenProject.getId().equals(journalEntry.getProjectId())) {

            gridTile.beginContext(journalEntry.getPosition(),
                    StructureLevel.PROJECT,
                    journalEntry.getProjectId(),
                    journalEntry.getProjectName());
        }
    }

    private void createProjectDoneIfSwitched(GridTile gridTile, JournalEntryEntity journalEntry, WorkContextReference lastOpenProject) {
        if (lastOpenProject != null && !lastOpenProject.getId().equals(journalEntry.getProjectId())) {

            gridTile.endContext(
                    journalEntry.getPosition(),
                    StructureLevel.PROJECT,
                    lastOpenProject.getId(),
                    lastOpenProject.getDescription(),
                    FinishTypeTag.Success);
        }
    }

    private void createTaskDoneIfSwitched(GridTile gridTile, JournalEntryEntity journalEntry, WorkContextReference lastOpenTask) {
        if (lastOpenTask != null && !lastOpenTask.getId().equals(journalEntry.getTaskId())) {
            gridTile.endContext(
                    journalEntry.getPosition(),
                    StructureLevel.TASK,
                    lastOpenTask.getId(),
                    lastOpenTask.getDescription(),
                    FinishTypeTag.Success);
        }
    }

    private void createIntentionStartAndEnd(GridTile gridTile, JournalEntryEntity journalEntry) {

        gridTile.beginContext(
                journalEntry.getPosition(),
                StructureLevel.INTENTION,
                journalEntry.getId(),
                journalEntry.getDescription());

        if (journalEntry.getFinishTime() != null) {

            gridTile.endContext(
                    journalEntry.getFinishTime(),
                    StructureLevel.INTENTION,
                    journalEntry.getId(),
                    journalEntry.getDescription(),
                    decodeFinishStatus(journalEntry.getFinishStatus()));
        }
    }

    private void createIntentionDoneIfNotNull(GridTile gridTile, JournalEntryEntity journalEntry, WorkContextReference lastIntentionStart) {

        if (lastIntentionStart != null) {
            gridTile.endContext(journalEntry.getPosition(),
                    StructureLevel.INTENTION,
                    lastIntentionStart.getId(),
                    lastIntentionStart.getDescription(),
                    FinishTypeTag.Success);
        }

    }

    private FinishTag decodeFinishStatus(String journalFinishStatus) {
        FinishTag finishTag = null;

        if (journalFinishStatus != null) {
            FinishStatus finishStatus = FinishStatus.valueOf(journalFinishStatus);

            switch (finishStatus) {
                case done:
                    finishTag = FinishTypeTag.Success;
                    break;
                case aborted:
                    finishTag = FinishTypeTag.Abort;
                    break;
            }
        }

        return finishTag;
    }
}
