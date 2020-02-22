package com.dreamscale.gridtime.core.machine.capabilities.cmd.returns;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;

public class CoordinateResults implements Results {

    private final String gridtimeCoords;

    public CoordinateResults(GeometryClock.GridTime gridTime) {
        this.gridtimeCoords = gridTime.toDisplayString();
    }

    @Override
    public String toDisplayString() {
        return gridtimeCoords;
    }
}
