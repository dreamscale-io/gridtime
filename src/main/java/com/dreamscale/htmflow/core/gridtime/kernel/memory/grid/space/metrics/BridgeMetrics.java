package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.space.metrics;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.CandleStick;

import java.util.List;

public class BridgeMetrics {

    private PlaceReference bridge;

    private int totalVisits = 0;

    private Integer visitsDuringWtf;

    private Integer visitsDuringLearning;

    private Integer visitsDuringRedToGreen;

    private CandleStick flamePerVisit;

    private CandleStick traversalSpeed;

    public BridgeMetrics(PlaceReference bridgeReference) {
        this.bridge = bridgeReference;
    }

    public void visit() {
        totalVisits++;
    }
}
