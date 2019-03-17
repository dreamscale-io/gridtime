package com.dreamscale.htmflow.core.feeds.clock;

import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;

public interface ClockChangeListener {

    void onClockTick(ZoomLevel zoomLevel) throws InterruptedException;
}
