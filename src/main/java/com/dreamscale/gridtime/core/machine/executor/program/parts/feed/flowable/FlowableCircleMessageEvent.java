package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable;

import com.dreamscale.gridtime.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.gridtime.core.machine.memory.feed.FlowableEvent;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;

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
    public <T> T get() {
        return (T)circleFeedMessageEntity;
    }

    @Override
    public LocalDateTime getPosition() {
        return circleFeedMessageEntity.getPosition();
    }

    @Override
    public String toDisplayString() {
        return "CircleMessage["+getPosition()+"]";
    }
}
