package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
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

    public BandLayerMapper(InnerGeometryClock internalClock, BandLayerType layerType) {
        this.internalClock = internalClock;
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public void startBand(LocalDateTime startBandPosition, Details details) {
        if (activeDetails != null) {
            TimeBand band = new TimeBand(activeBandStart, startBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = details;
        this.activeBandStart = startBandPosition;
    }

    public void clearBand(LocalDateTime endBandPosition) {
        if (activeDetails != null) {
            TimeBand band = new TimeBand(activeBandStart, endBandPosition, activeDetails);
            addTimeBand(band);
        }

        this.activeDetails = null;
        this.activeBandStart = endBandPosition;

    }

    public void finish() {
        if (activeDetails != null) {
            TimeBand band = new TimeBand(activeBandStart, internalClock.getToClockTime(), activeDetails);
            addTimeBand(band);
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



}
