package com.dreamscale.htmflow.core.feeds.executor.parts.loops;

import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.JournalFetcher;


public class LoopStrategyFactory {

    SearchSimilarPlaceStrategy searchSimilarPlaceStrategy;

    JournalFetcher journalFeedStrategy;

    public LoopStrategy get(StrategyType strategyType) {
        switch (strategyType) {
            case SEARCH_SIMILAR_PLACE:
                return searchSimilarPlaceStrategy;
        }
        return null;
    }

    public enum StrategyType {
        SEARCH_SIMILAR_PLACE,
        SPLIT_DIFFERENCES;
    }
}
