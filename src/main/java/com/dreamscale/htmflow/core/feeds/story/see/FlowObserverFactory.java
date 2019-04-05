package com.dreamscale.htmflow.core.feeds.story.see;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowObserverFactory {

    private final JournalContextObserver journalContextObserver;
    private final JournalFeelsObserver journalFeelsObserver;
    private final ExecutionRhythmObserver executionRhythmObserver;
    private final JournalAuthorObserver journalAuthorObserver;
    private final TroubleshootingStateObserver troubleshootingStateObserver;
    private final LearningStateObserver learningStateObserver;
    private final WTFScrapbookEventObserver wtfScrapbookMessageObserver;


    @Autowired
    private ComponentSpaceObserver componentSpaceObserver;

    FlowObserverFactory() {
        this.journalContextObserver = new JournalContextObserver();
        this.journalFeelsObserver = new JournalFeelsObserver();
        this.journalAuthorObserver = new JournalAuthorObserver();
        this.executionRhythmObserver = new ExecutionRhythmObserver();
        this.troubleshootingStateObserver = new TroubleshootingStateObserver();
        this.learningStateObserver = new LearningStateObserver();
        this.wtfScrapbookMessageObserver = new WTFScrapbookEventObserver();
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
            case TROUBLESHOOTING_STATE_OBSERVER:
                return troubleshootingStateObserver;
            case LEARNING_STATE_OBSERVER:
                return learningStateObserver;
            case WTF_SCRAPBOOK_MESSAGE_OBSERVER:
                return wtfScrapbookMessageObserver;

        }
        return null;
    }

    public enum ObserverType {
        JOURNAL_CONTEXT_OBSERVER,
        JOURNAL_FEELS_OBSERVER,
        COMPONENT_SPACE_OBSERVER,
        EXECUTION_RHYTHM_OBSERVER,
        JOURNAL_AUTHOR_OBSERVER,
        TROUBLESHOOTING_STATE_OBSERVER,
        LEARNING_STATE_OBSERVER,
        WTF_SCRAPBOOK_MESSAGE_OBSERVER;
    }
}
