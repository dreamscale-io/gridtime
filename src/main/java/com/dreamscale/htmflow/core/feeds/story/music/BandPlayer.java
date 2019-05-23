package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.timeband.Timeband;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.TimebandLayer;
import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;

import java.util.ArrayList;
import java.util.List;

public class BandPlayer implements Player {

    private final List<Timeband> playables;
    private final PlayListener playListener;
    private int index;


    public BandPlayer(TimebandLayer timeBandLayer, PlayListener playListener) {
        this.playables = timeBandLayer.getTimebands();
        this.playListener = playListener;
        this.index = 0;
    }

    @Override
    public void play(ClockBeat withinClockBeat) {
        List<Playable> beatContents = new ArrayList<>();

        for (int i = index; i < playables.size(); i++) {
            Timeband band = playables.get(i);

            if (band.getStartClockBeat().isWithin(withinClockBeat)) {
                beatContents.add(band);
                index++;
            } else {
                break;
            }
        }

       playListener.play(beatContents);
    }

}
