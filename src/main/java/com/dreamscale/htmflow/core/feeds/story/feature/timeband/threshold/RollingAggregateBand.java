package com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold;

import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.CandleStick;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBand;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class RollingAggregateBand extends TimeBand {


    private final CandleStickDetails candleSticks;

    public RollingAggregateBand(LocalDateTime start, LocalDateTime end) {
        super(start, end, new CandleStickDetails());

        this.candleSticks = (CandleStickDetails) super.getDetails();
    }

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
    public void aggregateWithPastObservations(RollingAggregateBand rollingAggregateBand) {

        LinkedList<CandleStick> priorSticks = rollingAggregateBand.getRolledPastCandlesMinusOldest();
        priorSticks.push(rollingAggregateBand.getWithinWindowCandleStick());

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

    public void evaluateThreshold() {

    }

    @Getter
    private static class CandleStickDetails extends Details {

        private CandleStick withinWindowCandleStick = new CandleStick();
        private CandleStick aggregateCandleStick = new CandleStick();
        private LinkedList<CandleStick> pastCandleSticks = new LinkedList<>(); //newest stick in the front, oldest in the back

        LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
            LinkedList<CandleStick> pastCandles = new LinkedList<>(pastCandleSticks);
            pastCandles.removeLast();

            return pastCandles;
        }

        void aggregateWithPastObservations(LinkedList<CandleStick> priorCandleSticks) {
            aggregateCandleStick = new CandleStick();

            pastCandleSticks = priorCandleSticks;

            for (CandleStick pastCandle: priorCandleSticks) {
                aggregateCandleStick.combineAggregate(pastCandle);
            }
        }
    }
}
