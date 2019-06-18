package com.dreamscale.htmflow.core.gridtime.executor.memory.feed;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Observable;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Bookmark;

import java.time.LocalDateTime;

public interface Flowable extends Observable {

    Bookmark getBookmark();

    <T> T get();

    boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);

    boolean hasTimeAfterWindow(LocalDateTime toClockPosition);

    Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition);

    Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);
}
