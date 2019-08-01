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
    private double weightedTotal;

    private double avg;

    private double stddev;
    private double min = Integer.MAX_VALUE;
    private double max = Integer.MIN_VALUE;

    @JsonIgnore
    private List<Double> data = new ArrayList<>();


    public void addWeightedMetricSample(WeightedMetric weightedMetric) {

        sampleCount++;

        totalDuration = totalDuration.plus(weightedMetric.getWeight());

        weightedTotal += weightedMetric.getCombinedMetricWeight();
        total += weightedMetric.getMetric();

        data.add(weightedMetric.getMetric());

        //get the lowest and highest, independent of weight

        min = Math.min(min, weightedMetric.getMetric());
        max = Math.max(max, weightedMetric.getMetric());

        //get the deviation of specific samples relative to the weighted average

        avg = weightedTotal / totalDuration.getSeconds();

        double squares = 0;
        for (Double aSample : data) {
            squares += Math.pow(aSample - avg, 2);
        }

        stddev = Math.sqrt(squares / sampleCount ) ;

    }


}
