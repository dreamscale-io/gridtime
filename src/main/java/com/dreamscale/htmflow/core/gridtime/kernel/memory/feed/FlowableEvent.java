package com.dreamscale.htmflow.core.gridtime.kernel.memory.feed;

import java.time.LocalDateTime;

public abstract class FlowableEvent implements Flowable {

    public abstract LocalDateTime getPosition();

    @Override
    public boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {
        return getPosition().isEqual(fromClockPosition) || (getPosition().isAfter(fromClockPosition) && getPosition().isBefore(toClockPosition));
    }

    @Override
    public boolean hasTimeAfterWindow(LocalDateTime toClockPosition) {
        return getPosition().isEqual(toClockPosition) || getPosition().isAfter(toClockPosition);
    }

    @Override
    public Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition) {
        return this;
    }

    @Override
    public Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {
        return this;
    }
}
