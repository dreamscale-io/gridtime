package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;

import java.time.LocalDateTime;

public class BandLayerMapper {

    private final BandLayerType layerType;
    private final MusicClock musicClock;
    private final TimebandLayer layer;
    private final FeatureFactory featureFactory;

    private Timeband carriedOverLastBand;

    private Details activeDetails;
    private LocalDateTime activeBandStart;
    private boolean isRollingBandLayer = false;

    public BandLayerMapper(FeatureFactory featureFactory, MusicClock musicClock, BandLayerType layerType) {
        this.featureFactory = featureFactory;
        this.musicClock = musicClock;
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
            Timeband band = featureFactory.createBand(layerType, activeBandStart, musicClock.getToClockTime(), activeDetails);
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
        this.activeBandStart = musicClock.getFromClockTime();
    }

    public Details getActiveDetails() {
        return activeDetails;
    }


    private void addTimeBand(Timeband timeBand) {
        timeBand.initCoordinates(musicClock);

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


    public void fillWithRollingAggregateBands(BeatSize beatSize) {
        isRollingBandLayer = true;

        int bandCount = BeatSize.BEAT.getBeatCount() / beatSize.getBeatCount();

        MusicClock.Beat startBeat = musicClock.getCurrentBeat();

        for (int i = 0; i < bandCount; i++) {
            MusicClock.Beat endBeat = musicClock.next(beatSize);

            RollingAggregateBand band = featureFactory.createRollingBand(layerType, startBeat.getClockTime(), endBeat.getClockTime());
            band.initCoordinates(musicClock);

            layer.add(band);

            startBeat = endBeat;
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

    public boolean isEmpty() {
        return layer.getTimebands().size() == 0;
    }
}
