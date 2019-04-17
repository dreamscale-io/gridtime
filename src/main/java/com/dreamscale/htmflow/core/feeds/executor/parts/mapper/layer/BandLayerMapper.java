package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.music.BeatsPerBucket;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;

import java.sql.Time;
import java.time.LocalDateTime;
import java.util.List;

public class BandLayerMapper {

    private final BandLayerType layerType;
    private final MusicGeometryClock internalClock;
    private final TimebandLayer layer;
    private final FeatureFactory featureFactory;

    private Timeband carriedOverLastBand;

    private Details activeDetails;
    private LocalDateTime activeBandStart;
    private boolean isRollingBandLayer = false;

    public BandLayerMapper(FeatureFactory featureFactory, MusicGeometryClock internalClock, BandLayerType layerType) {
        this.featureFactory = featureFactory;
        this.internalClock = internalClock;
        this.layerType = layerType;
        this.layer = featureFactory.createTimebandLayer(layerType);
    }

    public void startBand(LocalDateTime startBandPosition, Details details) {
        if (activeDetails != null) {

            Timeband band = featureFactory.createBand(layerType, activeBandStart, startBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = details;
        this.activeBandStart = startBandPosition;
    }

    public void clearBand(LocalDateTime endBandPosition) {
        if (activeDetails != null) {
            Timeband band = featureFactory.createBand(layerType, activeBandStart, endBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = null;
        this.activeBandStart = endBandPosition;

    }

    public void finish() {
        if (activeDetails != null) {
            Timeband band = featureFactory.createBand(layerType, activeBandStart, internalClock.getToClockTime(), activeDetails);
            addTimeBand(band);
        }

        if (isRollingBandLayer) {
            RollingAggregateBand lastRollingAggregate = (RollingAggregateBand)carriedOverLastBand;

            for (Timeband band : layer.getTimebands()) {
                RollingAggregateBand rollingBand = (RollingAggregateBand)band;

                rollingBand.aggregateWithPastObservations(lastRollingAggregate);
                rollingBand.evaluateThreshold();

                lastRollingAggregate = rollingBand;
            }
        }
    }

    public void initContext(Timeband lastBand, Details activeDetails) {
        this.carriedOverLastBand = lastBand;
        this.activeDetails = activeDetails;
        this.activeBandStart = internalClock.getFromClockTime();
    }

    public Details getActiveDetails() {
        return activeDetails;
    }


    private void addTimeBand(Timeband timeBand) {
        timeBand.initCoordinates(internalClock);

        layer.add(timeBand);
    }


    public BandLayerType getLayerType() {
        return layerType;
    }

    public Timeband getLastBand() {
        Timeband lastBand = layer.getLastBand();
        if (lastBand == null) {
            lastBand = carriedOverLastBand;
        }
        return lastBand;
    }


    public void fillWithRollingAggregateBands(BeatsPerBucket beatSize) {
        isRollingBandLayer = true;

        int bandCount = BeatsPerBucket.BEAT.getBeatCount() / beatSize.getBeatCount();

        MusicGeometryClock.Coords startCoords = internalClock.getCoordinates();

        for (int i = 0; i < bandCount; i++) {
            MusicGeometryClock.Coords endCoords = internalClock.panRight(beatSize);

            RollingAggregateBand band = featureFactory.createRollingBand(layerType, startCoords.getClockTime(), endCoords.getClockTime());
            layer.add(band);

            startCoords = endCoords;
        }
    }

    public boolean isRollingBandLayerConfigured() {
        return isRollingBandLayer;
    }

    public TimebandLayer getLayer() {
        return layer;
    }

    public void addRollingBandSample(LocalDateTime moment, double sample) {
        layer.addRollingBandSample(moment, sample);
    }
}
