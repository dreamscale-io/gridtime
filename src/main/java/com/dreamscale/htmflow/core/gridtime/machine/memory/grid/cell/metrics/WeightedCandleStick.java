package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Getter
@ToString
public class WeightedCandleStick extends MetricDistribution {

    private int sampleCount;
    private Duration totalDuration = Duration.ZERO;

    private double total;
    private double avg;
    private double stddev;
    private double min = Integer.MAX_VALUE;
    private double max = Integer.MIN_VALUE;

    @JsonIgnore
    private List<Double> data = new ArrayList<>();


    public void addWeightedMetricSample(WeightedMetric weightedMetric) {

        totalDuration = totalDuration.plus(weightedMetric.getDurationWeight());

        double secondsInState = weightedMetric.getSecondsInState();

        total += secondsInState;

        min = Math.min(min, secondsInState);
        max = Math.max(max, secondsInState);

        data.add(secondsInState);

        double squares = 0;
        for (Double aSample : data) {
            squares += Math.pow(aSample - avg, 2);
        }

        stddev = Math.sqrt(squares / (sampleCount+1) );
        avg = total / sampleCount;


        sampleCount++;
    }




}
