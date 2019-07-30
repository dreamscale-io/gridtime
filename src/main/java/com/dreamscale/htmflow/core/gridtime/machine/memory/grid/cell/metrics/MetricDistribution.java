package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public abstract class MetricDistribution {

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

    protected abstract int getSampleCount();

    protected abstract double getTotal();

    protected abstract double getStddev();

    protected abstract double getAvg();

    protected abstract double getMax();

    protected abstract double getMin();
}
