package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.band.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.FeelsContext;

import java.util.List;

/**
 * Translates the starting and stopping of modification activity, staring vs typing, into learning bands
 * when staring around, and considering progress and flow when regularly typing
 */
public class LearningStateObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                Integer flameRating = journalEntry.getFlameRating();
                if (flameRating != null && flameRating != 0) {
                    currentStoryTile.startBand(BandLayerType.FEELS, journalEntry.getPosition(), new FeelsContext(flameRating));
                } else {
                    currentStoryTile.clearBand(BandLayerType.FEELS, journalEntry.getPosition());
                }

            }
        }

        currentStoryTile.finishAfterLoad();

    }

}
