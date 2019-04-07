package com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable;

import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.feeds.common.FlowableEvent;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;

import java.time.LocalDateTime;

public class FlowableCircleMessageEvent extends FlowableEvent {

    private final CircleFeedMessageEntity circleFeedMessageEntity;

    public FlowableCircleMessageEvent(CircleFeedMessageEntity circleFeedMessageEntity) {
        this.circleFeedMessageEntity = circleFeedMessageEntity;
    }

    @Override
    public Bookmark getBookmark() {
        return new Bookmark(circleFeedMessageEntity.getPosition());
    }

    @Override
    public Object get() {
        return circleFeedMessageEntity;
    }

    @Override
    public LocalDateTime getPosition() {
        return circleFeedMessageEntity.getPosition();
    }

}
