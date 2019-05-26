package com.dreamscale.htmflow.core.feeds.common;

import java.time.LocalDateTime;

public interface Flow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

}
