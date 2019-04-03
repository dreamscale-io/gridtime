package com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable;

import com.dreamscale.htmflow.core.domain.JournalLinkEntity;
import com.dreamscale.htmflow.core.feeds.common.FlowableEvent;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;

import java.time.LocalDateTime;

public class FlowableJournalLinkEvent extends FlowableEvent {

    private final JournalLinkEntity journalLinkEntity;

    public FlowableJournalLinkEvent(JournalLinkEntity journalLinkEntity) {
        this.journalLinkEntity = journalLinkEntity;
    }

    @Override
    public Bookmark getBookmark() {
        return new Bookmark(journalLinkEntity.getPosition());
    }

    @Override
    public Object get() {
        return journalLinkEntity;
    }

    @Override
    public LocalDateTime getPosition() {
        return journalLinkEntity.getPosition();
    }
}
