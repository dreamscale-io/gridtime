package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimeBandLayer;

import java.util.ArrayList;
import java.util.List;

public class BandPlayer implements Player {

    private final List<TimeBand> playables;
    private final PlayListener playListener;



    public BandPlayer(TimeBandLayer timeBandLayer, PlayListener playListener) {
        this.playables = timeBandLayer.getTimeBands();
        this.playListener = playListener;
    }

    @Override
    public void play(MusicGeometryClock.Coords coords) {
        List<Playable> tickContents = new ArrayList<>();

        for (int i = 0; i < playables.size(); i++) {
            TimeBand band = playables.get(i);

            if (band.getStartCoords().isBeforeOrEqual(coords) && band.getEndCoords().isAfterOrEqual(coords)) {
                tickContents.add(band);
            } else {
                break;
            }
        }

       playListener.play(tickContents);
    }

}
