package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowObserverFactory {

    private final JournalContextObserver journalContextObserver;
    private final JournalFeelsObserver journalFeelsObserver;
    private final ExecutionRhythmObserver executionRhythmObserver;
    private final JournalAuthorObserver journalAuthorObserver;
    private final WTFStateObserver wtfStateObserver;
    private final LearningStateObserver learningStateObserver;
    private final CircleMessageEventObserver circleMessageEventObserver;


    @Autowired
    private ComponentSpaceObserver componentSpaceObserver;

    FlowObserverFactory() {
        this.journalContextObserver = new JournalContextObserver();
        this.journalFeelsObserver = new JournalFeelsObserver();
        this.journalAuthorObserver = new JournalAuthorObserver();
        this.executionRhythmObserver = new ExecutionRhythmObserver();
        this.wtfStateObserver = new WTFStateObserver();
        this.learningStateObserver = new LearningStateObserver();
        this.circleMessageEventObserver = new CircleMessageEventObserver();
    }

    public FlowObserver get(ObserverType observerType) {
        switch (observerType) {
            case JOURNAL_CONTEXT_OBSERVER:
                return journalContextObserver;
            case JOURNAL_FEELS_OBSERVER:
                return journalFeelsObserver;
            case JOURNAL_AUTHOR_OBSERVER:
                return journalAuthorObserver;
            case COMPONENT_SPACE_OBSERVER:
                return componentSpaceObserver;
            case EXECUTION_RHYTHM_OBSERVER:
                return executionRhythmObserver;
            case WTF_STATE_OBSERVER:
                return wtfStateObserver;
            case LEARNING_STATE_OBSERVER:
                return learningStateObserver;
            case CIRCLE_MESSAGE_OBSERVER:
                return circleMessageEventObserver;

        }
        return null;
    }

    public enum ObserverType {
        JOURNAL_CONTEXT_OBSERVER,
        JOURNAL_FEELS_OBSERVER,
        COMPONENT_SPACE_OBSERVER,
        EXECUTION_RHYTHM_OBSERVER,
        JOURNAL_AUTHOR_OBSERVER,
        WTF_STATE_OBSERVER,
        LEARNING_STATE_OBSERVER,
        CIRCLE_MESSAGE_OBSERVER;
    }
}
