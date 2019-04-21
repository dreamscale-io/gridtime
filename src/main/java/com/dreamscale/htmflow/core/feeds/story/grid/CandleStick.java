package com.dreamscale.htmflow.core.feeds.story.grid;

import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;

@Getter
@ToString
public class CandleStick {

    private int sampleCount;

    private double total;
    private double avg;
    private double stddev;
    private double min = Integer.MAX_VALUE;
    private double max = Integer.MIN_VALUE;

    private ArrayList<Double> data = new ArrayList<>();

    public void addSample(double sample) {
        avg = ((avg * sampleCount) + sample) / (sampleCount + 1);

        data.add(sample);

        double squares = 0;
        for (Double aSample : data) {
            squares += Math.pow(aSample - avg, 2);
        }

        stddev = Math.sqrt(squares / (sampleCount+1) );

        min = Math.min(min, sample);
        max = Math.max(max, sample);

        total += sample;
        sampleCount++;
    }

    public void combineAggregate(CandleStick candleStick) {
        if (candleStick == null) return;

        if (sampleCount + candleStick.sampleCount == 0) {
            return; //don't divide by 0
        }
        avg = ((avg * sampleCount) + (candleStick.avg * candleStick.sampleCount)) / (sampleCount + candleStick.sampleCount);

        data.addAll(candleStick.data);

        double squares = 0;
        for (Double aSample : data) {
            squares += Math.pow(aSample - avg, 2);
        }

        stddev = Math.sqrt(squares / (sampleCount + candleStick.sampleCount) );

        min = Math.min(min, candleStick.min);
        max = Math.max(max, candleStick.max);

        total = total + candleStick.total;
        sampleCount = sampleCount + candleStick.sampleCount;

    }

}
