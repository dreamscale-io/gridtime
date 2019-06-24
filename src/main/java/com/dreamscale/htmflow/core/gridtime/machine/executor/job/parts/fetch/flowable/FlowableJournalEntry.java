package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch.flowable;

import com.dreamscale.htmflow.core.domain.journal.JournalEntryEntity;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.FlowableEvent;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.source.Bookmark;

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
    public <T> T get() {
        return (T)journalEntry;
    }


    @Override
    public LocalDateTime getPosition() {
        return journalEntry.getPosition();
    }

    @Override
    public String toDisplayString() {
        return "JournalEntry["+getPosition()+"]";
    }
}
