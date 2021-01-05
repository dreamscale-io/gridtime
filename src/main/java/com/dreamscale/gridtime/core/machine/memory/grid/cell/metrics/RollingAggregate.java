package com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.util.LinkedList;

@Getter
public class RollingAggregate {

    private CandleStick withinWindowCandleStick = new CandleStick();
    private CandleStick aggregateCandleStick = new CandleStick();
    private LinkedList<CandleStick> pastCandleSticks = new LinkedList<>(); //newest stick in the front, oldest in the back

    private static final int MAX_STICKS = 4;
    /*
     * Add direct samples that fall within this time bucket
     */
    public void addSample(double sample) {
        withinWindowCandleStick.addSample(sample);
        aggregateCandleStick.addSample(sample);
    }

    /**
     * Combine with rolling aggregates from immediately prior rolling aggregate band
     */
    public void aggregateWithPastObservations(RollingAggregate rollingAggregate) {

        LinkedList<CandleStick> priorSticks = rollingAggregate.getRolledPastCandlesMinusOldest();
        priorSticks.push(rollingAggregate.getWithinWindowCandleStick());

        aggregateWithPastObservations(priorSticks);
    }

    @JsonIgnore
    private LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
        LinkedList<CandleStick> pastCandles = new LinkedList<>(pastCandleSticks);
        if (pastCandles.size() >= MAX_STICKS) {
            pastCandles.removeLast();
        }
        return pastCandles;
    }

    @JsonIgnore
    public boolean isTotalOverThreshold(int total) {
        return aggregateCandleStick.getTotal() > total;
    }


    private void aggregateWithPastObservations(LinkedList<CandleStick> priorCandleSticks) {
        aggregateCandleStick = new CandleStick();

        pastCandleSticks = priorCandleSticks;

        for (CandleStick pastCandle: priorCandleSticks) {
            aggregateCandleStick.combineAggregate(pastCandle);
        }

        aggregateCandleStick.combineAggregate(withinWindowCandleStick);
    }

}
