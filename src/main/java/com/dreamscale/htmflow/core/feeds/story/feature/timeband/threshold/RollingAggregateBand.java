package com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.DetailsType;
import com.dreamscale.htmflow.core.feeds.story.grid.CandleStick;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.LinkedList;

public class RollingAggregateBand extends Timeband {


    public RollingAggregateBand(LocalDateTime start, LocalDateTime end) {
        super(start, end, new CandleStickDetails());
        setFlowObjectType(FlowObjectType.ROLLING_TIMEBAND);

    }

    public RollingAggregateBand() {
        setFlowObjectType(FlowObjectType.ROLLING_TIMEBAND);
    }

    @JsonIgnore
    private CandleStickDetails getCandleSticks() {
        return (CandleStickDetails) super.getDetails();
    }

    /*
     * Add direct samples that fall within this time bucket
     */
    public void addSample(double sample) {
        getCandleSticks().getWithinWindowCandleStick().addSample(sample);
        getCandleSticks().getAggregateCandleStick().addSample(sample);
    }

    /**
     * Combine with rolling aggregates from immediately prior rolling aggregate band
     */
    public void aggregateWithPastObservations(RollingAggregateBand rollingAggregateBand) {

        LinkedList<CandleStick> priorSticks = rollingAggregateBand.getRolledPastCandlesMinusOldest();
        priorSticks.push(rollingAggregateBand.getWithinWindowCandleStick());

        getCandleSticks().aggregateWithPastObservations(priorSticks);
    }

    private LinkedList<CandleStick> getRolledPastCandlesMinusOldest() {
        return getCandleSticks().getRolledPastCandlesMinusOldest();
    }

    @JsonIgnore
    public CandleStick getWithinWindowCandleStick() {
        return getCandleSticks().getWithinWindowCandleStick();
    }

    @JsonIgnore
    public CandleStick getAggregateCandleStick() {
        return getCandleSticks().getAggregateCandleStick();
    }

    public void evaluateThreshold() {

    }

    @Getter
    public static class CandleStickDetails extends Details {

        private CandleStick withinWindowCandleStick = new CandleStick();
        private CandleStick aggregateCandleStick = new CandleStick();
        private LinkedList<CandleStick> pastCandleSticks = new LinkedList<>(); //newest stick in the front, oldest in the back

        public CandleStickDetails() {
            super(DetailsType.ROLLING_CANDLES);
        }

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
