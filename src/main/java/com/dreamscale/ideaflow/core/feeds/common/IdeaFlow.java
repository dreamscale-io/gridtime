package com.dreamscale.ideaflow.core.feeds.common;

import com.dreamscale.ideaflow.core.feeds.story.see.IdeaFlowObserver;

import java.time.LocalDateTime;

public interface IdeaFlow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

    void addFlowObserver(IdeaFlowObserver observer);
}
