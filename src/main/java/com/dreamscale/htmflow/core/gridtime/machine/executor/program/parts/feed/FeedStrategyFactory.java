package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed;

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
    CircleMessagesFeedStrategy circleMessagesFetcher;



    public FeedStrategy get(FeedType feedType) {
        switch (feedType) {
            case FILE_ACTIVITY_FEED:
                return fileActivityFeedStrategy;
            case JOURNAL_FEED:
                return journalFeedStrategy;
            case EXECUTION_ACTIVITY_FEED:
                return executionActivityFeedStrategy;
            case CIRCLE_MESSAGES_FEED:
                return circleMessagesFetcher;
        }
        return null;
    }

    public enum FeedType {
        JOURNAL_FEED,
        FILE_ACTIVITY_FEED,
        EXECUTION_ACTIVITY_FEED,
        CIRCLE_MESSAGES_FEED;
    }
}
