package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;

@Getter
@ToString
@AllArgsConstructor
public class WeightedMetric {

    private Duration weight;
    private double metric;

    public double getCombinedMetricWeight() {
        return weight.getSeconds() * metric;
    }
}
