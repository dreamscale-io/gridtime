package com.dreamscale.htmflow.core.feeds.story.mapper.layer;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBand;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TimeBandLayerMapper {

    private final TimeBandLayerType layerType;
    private final RelativeSequence relativeSequence;

    private TimeBand carriedOverLastBand;
    private List<TimeBand> bandsInWindow = new ArrayList<>();

    public TimeBandLayerMapper(TimeBandLayerType layerType) {
        this.relativeSequence = new RelativeSequence(1);
        this.layerType = layerType;
    }

    public InnerGeometryClock.Coords addTimeBand(InnerGeometryClock internalClock, TimeBand timeBand) {
        int nextSequence = relativeSequence.increment();

        timeBand.setCoordinates(internalClock.createCoords(timeBand.getMoment()));
        timeBand.setRelativeOffset(nextSequence);

        bandsInWindow.add(timeBand);

        return timeBand.getCoordinates();
    }

    public void repairSortingAndSequenceNumbers() {
        bandsInWindow.sort(new Comparator<TimeBand>() {
            @Override
            public int compare(TimeBand band1, TimeBand band2) {

                int compare = band1.getMoment().compareTo(band2.getMoment());

                if (compare == 0) {
                    compare = Integer.compare(band1.getRelativeOffset(), (band2.getRelativeOffset()));
                }

                return compare;
            }
        });

        //fix sequence numbers after resorting
        int sequence = 1;
        for (TimeBand timeBand : bandsInWindow) {
            timeBand.setRelativeOffset(sequence);
            sequence++;
        }
    }

    public TimeBandLayerType getLayerType() {
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

    public void initContext(TimeBand lastBand) {
        this.carriedOverLastBand = lastBand;
    }


}
