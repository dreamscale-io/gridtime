package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons;

import java.time.LocalDateTime;

public interface Flow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

}
