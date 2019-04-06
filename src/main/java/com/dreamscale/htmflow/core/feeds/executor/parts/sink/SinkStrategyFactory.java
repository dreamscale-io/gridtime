package com.dreamscale.htmflow.core.feeds.executor.parts.sink;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SinkStrategyFactory {

    @Autowired
    SaveToPostgresSink saveToPostgresSink;


    public SinkStrategy get(SinkType strategyType) {
        switch (strategyType) {
            case SAVE_TO_POSTGRES:
                return saveToPostgresSink;
        }
        return null;
    }

    public enum SinkType {
        SAVE_TO_POSTGRES,
        SAVE_TO_NEO4J;
    }
}
