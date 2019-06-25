package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SinkStrategyFactory {

    @Autowired
    SaveToPostgresSink saveToPostgresSink;

    @Autowired
    SaveBookmarkSink saveBookmarkSink;

    public SinkStrategy get(SinkType strategyType) {
        switch (strategyType) {
            case SAVE_TO_POSTGRES:
                return saveToPostgresSink;
            case SAVE_BOOKMARK:
                return saveBookmarkSink;
        }
        return null;
    }

    public enum SinkType {
        SAVE_TO_POSTGRES,
        SAVE_BOOKMARK;
    }
}
