package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.JournalFetcher;

public class SinkStrategyFactory {

    SaveStoryTileStrategy saveStoryTileStrategy;

    JournalFetcher journalFeedStrategy;

    public SinkStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case SAVE_STORY_FRAME:
                return saveStoryTileStrategy;
        }
        return null;
    }

    public enum StrategyType {
        SAVE_STORY_FRAME;
    }
}
