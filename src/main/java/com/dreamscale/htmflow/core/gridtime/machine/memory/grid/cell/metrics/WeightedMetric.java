package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.util.ArrayList;

@Getter
@ToString
@AllArgsConstructor
public class WeightedMetric {

    private Duration durationWeight;
    private double metric;

    public double getSecondsInState() {
        return durationWeight.getSeconds() * metric;
    }
}
