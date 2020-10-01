package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.MetricDistribution;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedList;

@Getter
@ToString
public class WindowMetric {

    private final int windowSize;

    private double total = 0;
    private double min = 0;
    private double avg;
    private double max;

    @JsonIgnore
    private LinkedList<Double> data = new LinkedList<>();

    public WindowMetric(int windowSize) {
        this.windowSize = windowSize;
    }

    public void addSample(double sample) {
        if (data.size() >= windowSize) {
            removeLastFromCalculations();
        }

        addNewSampleToCalculations(sample);

    }

    private void addNewSampleToCalculations(double sample) {
        data.add(sample);

        total += sample;
        avg = total / data.size();

        max = Math.max(max, sample);

        if (min == 0) {
            min = sample;
        } else {
            min = Math.min(min, sample);
        }
    }

    private void removeLastFromCalculations() {
        Double lastSample = data.removeLast();

        total -= lastSample;
        avg = total / data.size();

        if (lastSample.equals(max)) {
            max = 0;
            for (Double sample : data) {
                max = Math.max(max, sample);
            }
        }

        if (lastSample.equals(min) && data.size() > 0) {
            min = data.get(0);

            for (Double sample : data) {
                min = Math.min(min, sample);
            }
        }
    }


}
