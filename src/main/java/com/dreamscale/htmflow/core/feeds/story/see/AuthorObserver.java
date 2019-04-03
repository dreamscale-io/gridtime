package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableJournalEntry;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.feature.band.FeelsContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBand;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * When a person is pairing, generates multiple author references for the frame as a TimeBand,
 * otherwise generates a single author.  Attribution of authors is by Intention
 */
public class AuthorObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

    }

}
