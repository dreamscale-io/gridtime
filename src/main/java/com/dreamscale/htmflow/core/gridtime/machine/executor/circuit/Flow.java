package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import java.time.LocalDateTime;

public interface Flow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

}
