package com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics;

import lombok.Getter;

import java.util.LinkedList;

public class RollingAggregate {


    private CandleStickDetails candleSticks = new CandleStickDetails();

    /*
     * Add direct samples that fall within this time bucket
     */
    public void addSample(double sample) {
        candleSticks.getWithinWindowCandleStick().addSample(sample);
        candleSticks.getAggregateCandleStick().addSample(sample);
    }

    /**
     * Combine with rolling aggregates from immediately prior rolling aggregate band
     */
    public void aggregateWithPastObservations(RollingAggregate rollingAggregate) {

        LinkedList<CandleStick> priorSticks = rollingAggregate.getRolledPastCandlesMinusOldest();
        priorSticks.push(rollingAggregate.getWithinWindowCandleStick());

        candleSticks.aggregateWithPastObservations(priorSticks);
    }

    private LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
        return candleSticks.getRolledPastCandlesMinusOldest();
    }

    public CandleStick getWithinWindowCandleStick() {
        return candleSticks.getWithinWindowCandleStick();
    }

    public CandleStick getAggregateCandleStick() {
        return candleSticks.getAggregateCandleStick();
    }

    public boolean isTotalOverThreshold(int total) {
        return candleSticks.getAggregateCandleStick().getTotal() > total;
    }

    @Getter
    private static class CandleStickDetails {

        private CandleStick withinWindowCandleStick = new CandleStick();
        private CandleStick aggregateCandleStick = new CandleStick();
        private LinkedList<CandleStick> pastCandleSticks = new LinkedList<>(); //newest stick in the front, oldest in the back

        private static final int MAX_STICKS = 4;


        LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
            LinkedList<CandleStick> pastCandles = new LinkedList<>(pastCandleSticks);
            if (pastCandles.size() >= 4) {
                pastCandles.removeLast();
            }
            return pastCandles;
        }

        void aggregateWithPastObservations(LinkedList<CandleStick> priorCandleSticks) {
            aggregateCandleStick = new CandleStick();

            pastCandleSticks = priorCandleSticks;

            for (CandleStick pastCandle: priorCandleSticks) {
                aggregateCandleStick.combineAggregate(pastCandle);
            }

            aggregateCandleStick.combineAggregate(withinWindowCandleStick);
        }
    }
}
