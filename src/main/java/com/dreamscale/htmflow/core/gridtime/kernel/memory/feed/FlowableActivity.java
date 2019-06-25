package com.dreamscale.htmflow.core.gridtime.kernel.memory.feed;

import java.time.LocalDateTime;

public abstract class FlowableActivity implements Flowable {

    public abstract LocalDateTime getStart();

    public abstract LocalDateTime getEnd();

    public abstract void setStart(LocalDateTime newStart);

    public abstract void setEnd(LocalDateTime newEnd);

    public abstract FlowableActivity cloneActivity() throws CloneNotSupportedException;

    @Override
    public boolean hasTimeInWindow(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {

        return (getEnd().isAfter(fromClockPosition) && getStart().isBefore(toClockPosition));
    }

    @Override
    public boolean hasTimeAfterWindow(LocalDateTime toClockPosition) {
        return getEnd().isAfter(toClockPosition);
    }

    private FlowableActivity cloneAndCatchException() {
        FlowableActivity clone = null;
        try {
            clone = cloneActivity();

        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException(ex);
        }
        return clone;
    }

    @Override
    public Flowable splitTimeAfterWindowIntoNewFlowable(LocalDateTime toClockPosition) {

        FlowableActivity clone = cloneAndCatchException();
        clone.setStart(toClockPosition);

        return clone;
    }

    @Override
    public Flowable trimToFit(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {

        FlowableActivity clone = cloneAndCatchException();
        clone.setEnd(toClockPosition);

        return clone;
    }
}
