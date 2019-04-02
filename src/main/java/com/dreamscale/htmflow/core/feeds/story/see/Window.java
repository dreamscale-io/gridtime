package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.core.feeds.common.Flowable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Window {

    private final LocalDateTime fromClockPosition;
    private final LocalDateTime toClockPosition;
    private final ArrayList<Flowable> flowables;

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

    public List<Flowable> getFlowables() {
        return flowables;
    }

    public void addAll(List<Flowable> flowables) {
        this.flowables.addAll(flowables);
    }

    public void add(Flowable item) {
        this.flowables.add(item);
    }

    public void addAndTrimToFit(Flowable flowable) {
        Flowable trimmedFlowable = flowable.trimToFit(fromClockPosition, toClockPosition);
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

    public boolean hasTimeAfterWindow(Flowable flowable) {
        return flowable.hasTimeAfterWindow(toClockPosition);
    }

    public Flowable splitTimeAfterWindowIntoNewFlowable(Flowable flowable) {
        return flowable.splitTimeAfterWindowIntoNewFlowable(toClockPosition);
    }

    public boolean isNotEmpty() {
        return !flowables.isEmpty();
    }

    public boolean isWithin(LocalDateTime finishTime) {
        return finishTime.equals(fromClockPosition) || (finishTime.isAfter(fromClockPosition) && finishTime.isBefore(toClockPosition));
    }
}
