package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.story.see.FlowObserver;

import java.time.LocalDateTime;

public interface Flow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

    void addFlowObserver(FlowObserver observer);
}
