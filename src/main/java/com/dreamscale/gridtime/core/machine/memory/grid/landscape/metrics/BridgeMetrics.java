package com.dreamscale.gridtime.core.machine.memory.grid.landscape.metrics;

import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.CandleStick;

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
