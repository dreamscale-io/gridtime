package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.band.BandContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBand;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BandLayerMapper {

    private final BandLayerType layerType;
    private final RelativeSequence relativeSequence;
    private final InnerGeometryClock internalClock;

    private TimeBand carriedOverLastBand;
    private List<TimeBand> bandsInWindow = new ArrayList<>();

    private BandContext activeBandContext;
    private LocalDateTime activeBandStart;

    public BandLayerMapper(InnerGeometryClock internalClock, BandLayerType layerType) {
        this.internalClock = internalClock;
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public void startBand(LocalDateTime startBandPosition, BandContext bandContext) {
        if (activeBandContext != null) {
            TimeBand band = new TimeBand(activeBandStart, startBandPosition, activeBandContext);
            addTimeBand(band);
        }

        this.activeBandContext = bandContext;
        this.activeBandStart = startBandPosition;
    }

    public void clearBand(LocalDateTime endBandPosition) {
        if (activeBandContext != null) {
            TimeBand band = new TimeBand(activeBandStart, endBandPosition, activeBandContext);
            addTimeBand(band);
        }

        this.activeBandContext = null;
        this.activeBandStart = endBandPosition;

    }

    public void finish() {
        if (activeBandContext != null) {
            TimeBand band = new TimeBand(activeBandStart, internalClock.getToClockTime(), activeBandContext);
            addTimeBand(band);
        }
    }

    public void initContext(TimeBand lastBand, BandContext activeBandContext) {
        this.carriedOverLastBand = lastBand;
        this.activeBandContext = activeBandContext;
        this.activeBandStart = internalClock.getFromClockTime();
    }

    public BandContext getActiveBandContext() {
        return activeBandContext;
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



}
