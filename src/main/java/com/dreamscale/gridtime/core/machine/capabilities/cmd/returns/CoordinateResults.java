package com.dreamscale.gridtime.core.machine.capabilities.cmd.returns;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import lombok.Getter;

@Getter
public class CoordinateResults implements Results {

    private final GeometryClock.GridTime gridtime;

    public CoordinateResults(GeometryClock.GridTime gridTime) {
        this.gridtime = gridTime;
    }

    @Override
    public String toDisplayString() {
        return gridtime.toDisplayString();
    }
}
