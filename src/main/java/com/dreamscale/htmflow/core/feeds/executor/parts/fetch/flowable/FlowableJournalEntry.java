package com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable;

import com.dreamscale.htmflow.core.domain.JournalEntryEntity;
import com.dreamscale.htmflow.core.feeds.common.FlowableEvent;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;

import java.time.LocalDateTime;

public class FlowableJournalEntry extends FlowableEvent {

    private final JournalEntryEntity journalEntry;

    public FlowableJournalEntry(JournalEntryEntity journalEntryEntity) {
        this.journalEntry = journalEntryEntity;
    }

    @Override
    public Bookmark getBookmark() {
        return new Bookmark(journalEntry.getPosition());
    }

    @Override
    public Object get() {
        return journalEntry;
    }

    @Override
    public LocalDateTime getPosition() {
        return journalEntry.getPosition();
    }

}
