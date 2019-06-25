package com.dreamscale.htmflow.core.gridtime.kernel.executor.program;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;

import java.time.LocalDateTime;

public interface MetronomeProgram {

    LocalDateTime getStartPosition();

    boolean canTick(LocalDateTime nextPosition);

    void gotoPosition(GeometryClock.GridTime coords);

    TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);

    TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);
}
