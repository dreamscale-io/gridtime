package com.dreamscale.htmflow.core.feeds.executor.parts.fetch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FetchStrategyFactory {

    @Autowired
    FileActivityFetcher fileActivityFeedStrategy;

    @Autowired
    JournalFetcher journalFeedStrategy;

    @Autowired
    ExecutionActivityFetcher executionActivityFeedStrategy;


    public FetchStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case FILE_ACTIVITY_FEED:
                return fileActivityFeedStrategy;
            case JOURNAL_FEED:
                return journalFeedStrategy;
            case EXECUTION_ACTIVITY_FEED:
                return executionActivityFeedStrategy;
        }
        return null;
    }

    public enum StrategyType {
        JOURNAL_FEED,
        FILE_ACTIVITY_FEED,
        EXECUTION_ACTIVITY_FEED,
        WTF_EVENT_FEED;
    }
}
