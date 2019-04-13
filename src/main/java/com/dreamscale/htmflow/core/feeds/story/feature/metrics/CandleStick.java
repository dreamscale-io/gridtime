package com.dreamscale.htmflow.core.feeds.story.feature.metrics;

import lombok.Getter;

@Getter
public class CandleStick {

    private int sampleCount;

    private double total;
    private double avg;
    private double stddev;
    private double min;
    private double max;

    public void addSample(double sample) {
        avg = ((avg * sampleCount) + sample) / (sampleCount + 1);

        stddev = Math.sqrt(((Math.pow(stddev, 2) * sampleCount + Math.pow(sample - avg, 2)) / (sampleCount + 1)));

        min = Math.min(min, sample);
        max = Math.max(max, sample);

        total += sample;
        sampleCount++;
    }

    public void combineAggregate(CandleStick candleStick) {
        if (sampleCount + candleStick.sampleCount == 0) {
            return; //don't divide by 0
        }
        avg = ((avg * sampleCount) + (candleStick.avg * candleStick.sampleCount)) / (sampleCount + candleStick.sampleCount);

        stddev = Math.sqrt(((Math.pow(stddev, 2) * sampleCount) + (Math.pow(candleStick.stddev, 2) * candleStick.sampleCount))
                / (sampleCount + candleStick.sampleCount));

        min = Math.min(min, candleStick.min);
        max = Math.max(max, candleStick.max);

        total = total + candleStick.total;
        sampleCount = sampleCount + candleStick.sampleCount;

    }

}
