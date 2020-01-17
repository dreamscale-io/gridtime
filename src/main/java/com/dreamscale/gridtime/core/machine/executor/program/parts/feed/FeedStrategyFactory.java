package com.dreamscale.gridtime.core.machine.executor.program.parts.feed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FeedStrategyFactory {

    @Autowired
    FileActivityFeedStrategy fileActivityFeedStrategy;

    @Autowired
    JournalFeedStrategy journalFeedStrategy;

    @Autowired
    ExecutionActivityFeedStrategy executionActivityFeedStrategy;

    @Autowired
    WTFFeedMessagesFetcher wtfMessagesFetcher;



    public FeedStrategy get(FeedType feedType) {
        switch (feedType) {
            case FILE_ACTIVITY_FEED:
                return fileActivityFeedStrategy;
            case JOURNAL_FEED:
                return journalFeedStrategy;
            case EXECUTION_ACTIVITY_FEED:
                return executionActivityFeedStrategy;
            case WTF_MESSAGES_FEED:
                return wtfMessagesFetcher;
        }
        return null;
    }

    public enum FeedType {
        JOURNAL_FEED,
        FILE_ACTIVITY_FEED,
        EXECUTION_ACTIVITY_FEED,
        WTF_MESSAGES_FEED;
    }
}
