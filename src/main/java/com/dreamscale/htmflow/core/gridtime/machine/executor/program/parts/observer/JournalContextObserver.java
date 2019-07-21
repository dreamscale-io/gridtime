package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer;

import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.WorkContextEvent;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FinishTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

/**
 * Identifies the beginning of tasks and intentions as the beginning and ending of musical sequences
 */
public class JournalContextObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void see(Window<FlowableJournalEntry> window, FeaturePool featurePool) {

        GridTile gridTile = featurePool.getActiveGridTile();

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
