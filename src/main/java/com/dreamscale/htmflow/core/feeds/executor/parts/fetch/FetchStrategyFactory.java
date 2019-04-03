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

    @Autowired
    PairingEventFetcher pairingEventFeedStrategy;


    public FetchStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case FILE_ACTIVITY_FEED:
                return fileActivityFeedStrategy;
            case JOURNAL_FEED:
                return journalFeedStrategy;
            case EXECUTION_ACTIVITY_FEED:
                return executionActivityFeedStrategy;
            case PAIRING_EVENT_FEED:
                return  pairingEventFeedStrategy;
        }
        return null;
    }

    public enum StrategyType {
        JOURNAL_FEED,
        FILE_ACTIVITY_FEED,
        EXECUTION_ACTIVITY_FEED,
        PAIRING_EVENT_FEED,
        WTF_EVENT_FEED;
    }
}
