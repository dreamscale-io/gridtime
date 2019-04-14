package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;

import java.util.ArrayList;
import java.util.List;

public class BandPlayer implements Player {

    private final List<Timeband> playables;
    private final PlayListener playListener;



    public BandPlayer(TimebandLayer timeBandLayer, PlayListener playListener) {
        this.playables = timeBandLayer.getTimebands();
        this.playListener = playListener;
    }

    @Override
    public void play(MusicGeometryClock.Coords coords) {
        List<Playable> tickContents = new ArrayList<>();

        for (int i = 0; i < playables.size(); i++) {
            Timeband band = playables.get(i);

            if (band.getStartCoords().isBeforeOrEqual(coords) && band.getEndCoords().isAfterOrEqual(coords)) {
                tickContents.add(band);
            } else {
                break;
            }
        }

       playListener.play(tickContents);
    }

}
