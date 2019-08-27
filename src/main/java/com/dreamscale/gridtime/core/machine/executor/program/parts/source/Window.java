package com.dreamscale.gridtime.core.machine.executor.program.parts.source;

import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Window<T extends Flowable> {

    private final LocalDateTime fromClockPosition;
    private final LocalDateTime toClockPosition;
    private final List<T> flowables;

    private boolean isFinished;

    public Window(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) {
        this.fromClockPosition = fromClockPosition;
        this.toClockPosition = toClockPosition;
        this.flowables = new ArrayList<>();
        this.isFinished = false;
    }

    public LocalDateTime getStart() {
        return fromClockPosition;
    }

    public LocalDateTime getEnd() {
        return toClockPosition;
    }

    public Duration getDuration() {
        return Duration.between(getStart(), getEnd());
    }

    public List<T> getFlowables() {
        return flowables;
    }

    public void addAll(List<T> flowables) {
        this.flowables.addAll(flowables);
    }

    public void add(T item) {
        this.flowables.add(item);
    }

    public void addAndTrimToFit(T flowable) {
        T trimmedFlowable = (T) flowable.trimToFit(fromClockPosition, toClockPosition);
        this.flowables.add(trimmedFlowable);
    }

    public void setFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }

    public boolean isFinished() {
        return isFinished;
    }


    public boolean hasTimeInWindow(Flowable flowable) {
        return flowable.hasTimeInWindow(fromClockPosition, toClockPosition);
    }

    public boolean hasTimeAfterWindow(T flowable) {
        return flowable.hasTimeAfterWindow(toClockPosition);
    }

    public T splitTimeAfterWindowIntoNewFlowable(T flowable) {
        return (T) flowable.splitTimeAfterWindowIntoNewFlowable(toClockPosition);
    }

    public boolean isNotEmpty() {
        return !flowables.isEmpty();
    }

    public boolean isWithin(LocalDateTime finishTime) {
        return finishTime.equals(fromClockPosition) || (finishTime.isAfter(fromClockPosition) && finishTime.isBefore(toClockPosition));
    }
}
