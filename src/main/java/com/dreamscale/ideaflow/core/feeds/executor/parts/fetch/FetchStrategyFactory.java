package com.dreamscale.ideaflow.core.feeds.executor.parts.fetch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchStrategyFactory {

    @Autowired
    FileActivityFetcher fileActivityFeedStrategy;

    @Autowired
    JournalFetcher journalFeedStrategy;

    public FetchStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case FILE_ACTIVITY_FEED:
                return fileActivityFeedStrategy;
            case JOURNAL_FEED:
                return journalFeedStrategy;
        }
        return null;
    }

    public enum StrategyType {
        JOURNAL_FEED,
        FILE_ACTIVITY_FEED,
        EXECUTION_ACTIVITY_FEED;
    }
}
