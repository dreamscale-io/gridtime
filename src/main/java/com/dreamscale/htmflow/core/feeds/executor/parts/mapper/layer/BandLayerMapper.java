package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.clock.BeatsPerBucket;
import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBand;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BandLayerMapper {

    private final BandLayerType layerType;
    private final RelativeSequence relativeSequence;
    private final InnerGeometryClock internalClock;

    private TimeBand carriedOverLastBand;
    private List<TimeBand> bandsInWindow = new ArrayList<>();

    private Details activeDetails;
    private LocalDateTime activeBandStart;
    private boolean isRollingBandLayer = false;

    public BandLayerMapper(InnerGeometryClock internalClock, BandLayerType layerType) {
        this.internalClock = internalClock;
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public void startBand(LocalDateTime startBandPosition, Details details) {
        if (activeDetails != null) {

            TimeBand band = BandFactory.create(layerType, activeBandStart, startBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = details;
        this.activeBandStart = startBandPosition;
    }

    public void clearBand(LocalDateTime endBandPosition) {
        if (activeDetails != null) {
            TimeBand band = BandFactory.create(layerType, activeBandStart, endBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = null;
        this.activeBandStart = endBandPosition;

    }

    public void finish() {
        if (activeDetails != null) {
            TimeBand band = BandFactory.create(layerType, activeBandStart, internalClock.getToClockTime(), activeDetails);
            addTimeBand(band);
        }

        if (isRollingBandLayer) {
            RollingAggregateBand lastRollingAggregate = (RollingAggregateBand)carriedOverLastBand;

            for (TimeBand band : bandsInWindow) {
                RollingAggregateBand rollingBand = (RollingAggregateBand)band;

                rollingBand.aggregateWithPastObservations(lastRollingAggregate);
                rollingBand.evaluateThreshold();

                lastRollingAggregate = rollingBand;
            }
        }
    }

    public void initContext(TimeBand lastBand, Details activeDetails) {
        this.carriedOverLastBand = lastBand;
        this.activeDetails = activeDetails;
        this.activeBandStart = internalClock.getFromClockTime();
    }

    public Details getActiveDetails() {
        return activeDetails;
    }


    private void addTimeBand(TimeBand timeBand) {
        timeBand.initCoordinates(internalClock);

        int nextSequence = relativeSequence.increment();
        timeBand.setRelativeOffset(nextSequence);

        bandsInWindow.add(timeBand);
    }


    public BandLayerType getLayerType() {
        return layerType;
    }

    public List<TimeBand> getTimeBands() {
        return bandsInWindow;
    }

    public TimeBand getLastBand() {
        TimeBand lastBand = null;

        if (bandsInWindow.size() > 1) {
            lastBand = bandsInWindow.get(bandsInWindow.size() - 1);
        }

        if (lastBand == null) {
            lastBand = carriedOverLastBand;
        }

        return lastBand;
    }


    public void fillWithRollingAggregateBands(BeatsPerBucket beatSize) {
        isRollingBandLayer = true;

        int bandCount = BeatsPerBucket.BEAT.getBeatCount() / beatSize.getBeatCount();

        InnerGeometryClock.Coords startCoords = internalClock.getCoordinates();

        for (int i = 0; i < bandCount; i++) {
            InnerGeometryClock.Coords endCoords = startCoords.panRight(beatSize);

            RollingAggregateBand band = BandFactory.createRollingBand(layerType, startCoords.getClockTime(), endCoords.getClockTime());
            bandsInWindow.add(band);

            startCoords = endCoords;
        }
    }

    public void addRollingBandSample(LocalDateTime moment, double sample) {
        for (TimeBand band : bandsInWindow) {
            RollingAggregateBand rollingBand = (RollingAggregateBand)band;

            if (rollingBand.contains(moment)) {
                rollingBand.addSample(sample);
                break;
            }

        }
    }

    public boolean isRollingBandLayerConfigured() {
        return isRollingBandLayer;
    }
}
