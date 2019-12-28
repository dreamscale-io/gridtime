package com.dreamscale.gridtime.core.machine.executor.program.parts.feed.flowable;

import com.dreamscale.gridtime.core.domain.circuit.message.WTFFeedMessageEntity;
import com.dreamscale.gridtime.core.machine.memory.feed.FlowableEvent;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;

import java.time.LocalDateTime;

public class FlowableCircuitWTFMessageEvent extends FlowableEvent {

    private final WTFFeedMessageEntity wtfFeedMessageEntity;

    public FlowableCircuitWTFMessageEvent(WTFFeedMessageEntity wtfFeedMessageEntity) {
        this.wtfFeedMessageEntity = wtfFeedMessageEntity;
    }

    @Override
    public Bookmark getBookmark() {
        return new Bookmark(wtfFeedMessageEntity.getPosition());
    }

    @Override
    public <T> T get() {
        return (T)wtfFeedMessageEntity;
    }

    @Override
    public LocalDateTime getPosition() {
        return wtfFeedMessageEntity.getPosition();
    }

    @Override
    public String toDisplayString() {
        return "CircuitWTFMessage["+getPosition()+"]";
    }
}
