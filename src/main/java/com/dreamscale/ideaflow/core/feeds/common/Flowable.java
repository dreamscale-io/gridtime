package com.dreamscale.ideaflow.core.feeds.common;

import com.dreamscale.ideaflow.core.feeds.executor.parts.source.Bookmark;

import java.time.LocalDateTime;

public interface Flowable {

    Bookmark getBookmark();

    Object get();

    boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);

    boolean hasTimeAfterWindow(LocalDateTime toClockPosition);

    Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition);

    Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);
}
