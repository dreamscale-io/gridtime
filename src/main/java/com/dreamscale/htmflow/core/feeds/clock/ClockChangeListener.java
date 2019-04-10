package com.dreamscale.htmflow.core.feeds.clock;

public interface ClockChangeListener {

    void onClockTick(ZoomLevel zoomLevel) throws InterruptedException;
}
