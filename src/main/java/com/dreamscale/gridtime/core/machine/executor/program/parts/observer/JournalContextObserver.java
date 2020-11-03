package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.domain.flow.FinishStatus;
import com.dreamscale.gridtime.core.domain.journal.JournalEntryEntity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable.FlowableJournalEntry;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feature.details.WorkContextEvent;
import com.dreamscale.gridtime.core.machine.memory.tag.FinishTag;
import com.dreamscale.gridtime.core.machine.memory.tag.types.FinishTypeTag;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of musical sequences
 */
public class JournalContextObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void observe(Window<FlowableJournalEntry> window, GridTile gridTile) {

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

                if (journalEntry.getFinishTime() != null) {
                    gridTile.clearWorkContext(journalEntry.getFinishTime(), decodeFinishStatus(journalEntry.getFinishStatus()));
                }
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
