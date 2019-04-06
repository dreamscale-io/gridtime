package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class RollingAggregateBand extends TimeBand {

    private CandleStick withinWindowCandleStick;
    private CandleStick aggregateCandleStick;
    private LinkedList<CandleStick> pastCandleSticks; //newest stick in the front, oldest in the back

    public RollingAggregateBand(LocalDateTime start, LocalDateTime end) {
        super(start, end, null);

        this.withinWindowCandleStick = new CandleStick();
        this.aggregateCandleStick = new CandleStick();
        this.pastCandleSticks = new LinkedList<>();
    }

    /*
     * Add direct samples that fall within this time bucket
     */
    public void addSample(double sample) {
        this.withinWindowCandleStick.addSample(sample);
        this.aggregateCandleStick.addSample(sample);
    }

    /**
     * Combine with rolling aggregates from immediately prior rolling aggregate band
     */
    public void aggregateWithPastObservations(RollingAggregateBand rollingAggregateBand) {
        aggregateCandleStick = new CandleStick();

        pastCandleSticks = rollingAggregateBand.getRolledPastCandlesMinusOldest();
        pastCandleSticks.push(rollingAggregateBand.getWithinWindowCandleStick());

        for (CandleStick pastCandle: pastCandleSticks) {
            aggregateCandleStick.combineAggregate(pastCandle);
        }
    }

    private LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
        LinkedList<CandleStick> pastCandles = new LinkedList<>(pastCandleSticks);
        pastCandles.removeLast();

        return pastCandles;
    }

    public CandleStick getWithinWindowCandleStick() {
        return withinWindowCandleStick;
    }

    public CandleStick getAggregateCandleStick() {
        return aggregateCandleStick;
    }


    public boolean contains(LocalDateTime moment) {
        return (moment.isEqual(getStart()) || moment.isAfter(getStart())) && moment.isBefore(getEnd());
    }
}
