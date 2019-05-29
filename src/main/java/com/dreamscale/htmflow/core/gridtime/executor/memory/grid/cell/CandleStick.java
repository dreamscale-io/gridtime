package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private ArrayList<Double> data = new ArrayList<>();

    public double getValueByAggregateType(AggregateType aggregateType) {
        switch (aggregateType) {
            case MIN:
                return getMin();
            case MAX:
                return getMax();
            case AVG:
                return getAvg();
            case STDDEV:
                return getStddev();
            case TOTAL:
                return getTotal();
            case COUNT:
                return getSampleCount();
        }
        return 0;
    }

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
