package com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit;

import java.time.LocalDateTime;

public interface Flow {

    void tick(LocalDateTime fromClockPosition, LocalDateTime toClockPosition) throws InterruptedException;

}
