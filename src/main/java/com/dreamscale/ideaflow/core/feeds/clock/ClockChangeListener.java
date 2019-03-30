package com.dreamscale.ideaflow.core.feeds.clock;

import com.dreamscale.ideaflow.core.feeds.common.ZoomLevel;

public interface ClockChangeListener {

    void onClockTick(ZoomLevel zoomLevel) throws InterruptedException;
}
