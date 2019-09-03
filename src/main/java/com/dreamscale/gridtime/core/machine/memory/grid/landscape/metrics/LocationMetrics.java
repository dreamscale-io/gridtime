package com.dreamscale.gridtime.core.machine.memory.grid.landscape.metrics;

import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;

import java.time.Duration;

public class LocationMetrics {

    private PlaceReference location;

    private GridMetrics gridMetrics;

    public LocationMetrics(PlaceReference locationReference, GridMetrics boxMetrics) {
        this.location = locationReference;
        this.gridMetrics = new GridMetrics(boxMetrics);
    }

    public void visit(Duration timeInLocation) {
        gridMetrics.addVelocitySample(timeInLocation);
    }
}
