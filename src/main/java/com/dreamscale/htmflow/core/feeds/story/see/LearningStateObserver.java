package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.band.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.FeelsContext;

import java.util.List;

/**
 * Translates the starting and stopping of modification activity, staring vs typing, into learning bands
 * when staring around, and considering progress and flow when regularly typing
 */
public class LearningStateObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableJournalEntry) {
                JournalEntryEntity journalEntry = ((JournalEntryEntity) flowable.get());

                Integer flameRating = journalEntry.getFlameRating();
                if (flameRating != null && flameRating != 0) {
                    currentStoryFrame.startBand(BandLayerType.FEELS, journalEntry.getPosition(), new FeelsContext(flameRating));
                } else {
                    currentStoryFrame.clearBand(BandLayerType.FEELS, journalEntry.getPosition());
                }

            }
        }

        currentStoryFrame.finishAfterLoad();

    }

}
