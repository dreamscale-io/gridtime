package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.WorkContextEvent;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.WorkContextReference;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.StructureLevel;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of musical sequences
 */
public class JournalContextObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void see(Window<FlowableJournalEntry> window, GridTile gridTile) {


        for (FlowableJournalEntry flowable : window.getFlowables()) {
                JournalEntryEntity journalEntry = flowable.get();

            WorkContextEvent workContextEvent = new WorkContextEvent(
                    journalEntry.getId(),
                    journalEntry.getDescription(),
                    journalEntry.getTaskId(),
                    journalEntry.getTaskName(),
                    journalEntry.getProjectId(),
                    journalEntry.getProjectName());

                gridTile.startWorkContext(journalEntry.getPosition(), workContextEvent);
                gridTile.clearWorkContext(journalEntry.getFinishTime(), decodeFinishStatus(journalEntry.getFinishStatus()));
        }

    }


    private FinishTag decodeFinishStatus(String journalFinishStatus) {
        FinishTag finishTag = FinishTypeTag.Success;

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
