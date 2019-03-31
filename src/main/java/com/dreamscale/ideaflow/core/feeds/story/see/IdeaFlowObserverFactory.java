package com.dreamscale.ideaflow.core.feeds.story.see;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IdeaFlowObserverFactory {

    private final JournalContextObserver journalContextObserver;

    @Autowired
    private ComponentSpaceObserver componentSpaceObserver;

    IdeaFlowObserverFactory() {
        this.journalContextObserver = new JournalContextObserver();
    }

    public IdeaFlowObserver get(ObserverType observerType) {
        switch (observerType) {
            case JOURNAL_CONTEXT_OBSERVER:
                return journalContextObserver;
            case COMPONENT_SPACE_OBSERVER:
                return componentSpaceObserver;
        }
        return null;
    }

    public enum ObserverType {
        JOURNAL_CONTEXT_OBSERVER,
        COMPONENT_SPACE_OBSERVER;
    }
}
