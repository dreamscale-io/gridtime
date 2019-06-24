package com.dreamscale.htmflow.core.gridtime.machine.executor.job;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;

import java.time.LocalDateTime;

public interface MetronomeJob {

    LocalDateTime getStartPosition();

    boolean canTick(LocalDateTime nextPosition);

    void gotoPosition(GeometryClock.GridTime coords);

    TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);

    TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);
}
