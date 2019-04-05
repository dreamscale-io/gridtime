package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.details.FeelsDetails;

import java.util.List;

/**
 * Translates the flame ratings on JournalEntries to TimeBands with a FeelsContext that always fits within
 * the frame and contributes to the push/pull signals generated by all the flames combined
 */
public class JournalFeelsObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                Integer flameRating = journalEntry.getFlameRating();
                if (flameRating != null && flameRating != 0) {
                    currentStoryTile.startBand(BandLayerType.FEELS, journalEntry.getPosition(), new FeelsDetails(flameRating));
                } else {
                    currentStoryTile.clearBand(BandLayerType.FEELS, journalEntry.getPosition());
                }

            }
        }

        currentStoryTile.finishAfterLoad();

    }

}
