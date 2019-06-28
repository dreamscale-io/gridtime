package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.space.metrics;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.GridMetrics;

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
