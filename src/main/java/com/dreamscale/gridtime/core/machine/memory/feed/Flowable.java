package com.dreamscale.gridtime.core.machine.memory.feed;

import com.dreamscale.gridtime.api.grid.Observable;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Bookmark;

import java.time.LocalDateTime;

public interface Flowable extends Observable {

    Bookmark getBookmark();

    <T> T get();

    boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);

    boolean hasTimeAfterWindow(LocalDateTime toClockPosition);

    Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition);

    Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition);
}
