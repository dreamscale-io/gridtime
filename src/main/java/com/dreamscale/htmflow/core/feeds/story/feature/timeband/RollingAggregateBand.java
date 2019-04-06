package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class RollingAggregateBand extends TimeBand {

    private CandleStick newestCandleStick;
    private CandleStick aggregateCandleStick;
    private LinkedList<CandleStick> pastCandleSticks; //newest stick in the front, oldest in the back

    public RollingAggregateBand(LocalDateTime start, LocalDateTime end, Details details) {
        super(start, end, details);

        this.newestCandleStick = new CandleStick();
        this.aggregateCandleStick = new CandleStick();
        this.pastCandleSticks = new LinkedList<>();
    }

    /*
     * Add direct samples that fall within this time bucket
     */
    public void addSample(double sample) {
        this.newestCandleStick.addSample(sample);
        this.aggregateCandleStick.addSample(sample);
    }

    /**
     * Combine with rolling aggregates from immediately prior rolling aggregate band
     */
    public void aggregateWithPastObservations(RollingAggregateBand rollingAggregateBand) {
        aggregateCandleStick = new CandleStick();

        pastCandleSticks = rollingAggregateBand.getRolledPastCandlesMinusOldest();
        pastCandleSticks.push(rollingAggregateBand.getNewestCandleStick());

        for (CandleStick pastCandle: pastCandleSticks) {
            aggregateCandleStick.combineAggregate(pastCandle);
        }
    }

    private LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
        LinkedList<CandleStick> pastCandles = new LinkedList<>(pastCandleSticks);
        pastCandles.removeLast();

        return pastCandles;
    }

    public CandleStick getNewestCandleStick() {
        return newestCandleStick;
    }

    public CandleStick getAggregateCandleStick() {
        return aggregateCandleStick;
    }


}
