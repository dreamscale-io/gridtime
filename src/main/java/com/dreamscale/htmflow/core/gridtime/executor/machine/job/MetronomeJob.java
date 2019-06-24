package com.dreamscale.htmflow.core.gridtime.executor.machine.job;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;

public interface MetronomeJob {

    void gotoPosition(GeometryClock.GridTime coords);

    TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);

    TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime);
}
