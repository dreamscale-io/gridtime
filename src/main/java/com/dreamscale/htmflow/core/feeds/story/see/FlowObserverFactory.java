package com.dreamscale.htmflow.core.feeds.story.see;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowObserverFactory {

    private final JournalContextObserver journalContextObserver;
    private final JournalFeelsObserver journalFeelsObserver;
    private final ExecutionRhythmObserver executionRhythmObserver;

    @Autowired
    private ComponentSpaceObserver componentSpaceObserver;

    FlowObserverFactory() {
        this.journalContextObserver = new JournalContextObserver();
        this.journalFeelsObserver = new JournalFeelsObserver();
        this.executionRhythmObserver = new ExecutionRhythmObserver();
    }

    public FlowObserver get(ObserverType observerType) {
        switch (observerType) {
            case JOURNAL_CONTEXT_OBSERVER:
                return journalContextObserver;
            case JOURNAL_FEELS_OBSERVER:
                return journalFeelsObserver;
            case COMPONENT_SPACE_OBSERVER:
                return componentSpaceObserver;
            case EXECUTION_RHYTHM_OBSERVER:
                return executionRhythmObserver;
        }
        return null;
    }

    public enum ObserverType {
        JOURNAL_CONTEXT_OBSERVER,
        JOURNAL_FEELS_OBSERVER,
        COMPONENT_SPACE_OBSERVER,
        EXECUTION_RHYTHM_OBSERVER;
    }
}
