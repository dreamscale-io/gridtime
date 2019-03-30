package com.dreamscale.ideaflow.core.feeds.executor.parts.sink;

import com.dreamscale.ideaflow.core.feeds.executor.parts.fetch.JournalFetcher;

public class SinkStrategyFactory {

    SaveStoryFrameStrategy saveStoryFrameStrategy;

    JournalFetcher journalFeedStrategy;

    public SinkStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case SAVE_STORY_FRAME:
                return saveStoryFrameStrategy;
        }
        return null;
    }

    public enum StrategyType {
        SAVE_STORY_FRAME;
    }
}
