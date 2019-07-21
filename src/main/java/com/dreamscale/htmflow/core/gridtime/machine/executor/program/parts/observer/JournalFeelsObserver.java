package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

/**
 * Translates the flame ratings on JournalEntries to TimeBands with a FeelsContext that always fits within
 * the frame and contributes to the push/pull signals generated by all the flames combined
 */
public class JournalFeelsObserver implements FlowObserver<FlowableJournalEntry> {

    @Override
    public void see(Window<FlowableJournalEntry> window, FeaturePool featurePool) {

        GridTile gridTile = featurePool.getActiveGridTile();

        for (FlowableJournalEntry flowable : window.getFlowables()) {
            JournalEntryEntity journalEntry = (flowable.get());

            Integer flameRating = journalEntry.getFlameRating();
            if (flameRating != null && flameRating != 0) {
                gridTile.startFeelsBand(journalEntry.getPosition(), flameRating);
            } else {
                gridTile.clearFeelsBand(journalEntry.getPosition());
            }
        }

    }

}
