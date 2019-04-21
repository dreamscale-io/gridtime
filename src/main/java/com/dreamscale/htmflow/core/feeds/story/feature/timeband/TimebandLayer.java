package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.RollingAggregateBand;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TimebandLayer extends FlowFeature {

    private final List<Timeband> timebands = new ArrayList<>();
    private final BandLayerType layerType;

    public TimebandLayer(BandLayerType layerType) {
        this.layerType = layerType;
    }

    public List<Timeband> getTimebands() {
        return timebands;
    }

    public BandLayerType getLayerType() {
        return layerType;
    }

    public void add(Timeband timeBand) {
        timeBand.initRelativeSequence(this, timebands.size() + 1);
        this.timebands.add(timeBand);
    }

    public void addRollingBandSample(LocalDateTime moment, double sample) {
        for (Timeband band : timebands) {
            RollingAggregateBand rollingBand = (RollingAggregateBand)band;

            if (rollingBand.contains(moment)) {
                rollingBand.addSample(sample);
                break;
            }

        }
    }

    public Timeband getLastBand() {
        Timeband lastBand = null;

        if (timebands.size() > 1) {
            lastBand = timebands.get(timebands.size() - 1);
        }

        return lastBand;
    }
}
