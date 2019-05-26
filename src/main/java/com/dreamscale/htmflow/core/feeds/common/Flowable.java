package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.executor.parts.source.Bookmark;

import java.time.LocalDateTime;

public interface Flowable {

    Bookmark getBookmark();

    <T> T get();

    boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);

    boolean hasTimeAfterWindow(LocalDateTime toClockPosition);

    Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition);

    Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);
}
