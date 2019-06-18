package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import org.springframework.stereotype.Component;

@Component
public class FlowObserverFactory {

    private final JournalContextObserver journalContextObserver;
    private final JournalFeelsObserver journalFeelsObserver;
    private final ExecutionRhythmObserver executionRhythmObserver;
    private final WTFStateObserver wtfStateObserver;
    private final ComponentSpaceObserver componentSpaceObserver;
    private final JournalAuthorObserver journalAuthorObserver;

    FlowObserverFactory() {
        this.journalContextObserver = new JournalContextObserver();
        this.journalFeelsObserver = new JournalFeelsObserver();
        this.executionRhythmObserver = new ExecutionRhythmObserver();
        this.wtfStateObserver = new WTFStateObserver();
        this.componentSpaceObserver = new ComponentSpaceObserver();
        this.journalAuthorObserver = new JournalAuthorObserver();
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

        }
        return null;
    }

    public enum ObserverType {
        JOURNAL_CONTEXT_OBSERVER,
        JOURNAL_FEELS_OBSERVER,
        COMPONENT_SPACE_OBSERVER,
        EXECUTION_RHYTHM_OBSERVER,
        JOURNAL_AUTHOR_OBSERVER,
        WTF_STATE_OBSERVER;
    }
}
