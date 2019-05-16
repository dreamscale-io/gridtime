package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;

import java.util.ArrayList;
import java.util.List;

public class RhythmPlayer implements Player {

    private final List<? extends Playable> playables;
    private final PlayListener playListener;

    private int index;

    public RhythmPlayer(RhythmLayer rhythmLayer, PlayListener playListener) {
        this.index = 0;
        this.playables = rhythmLayer.getMovements();
        this.playListener = playListener;
    }


    @Override
    public void play(MusicClock.Beat withinBeat) {
        List<Playable> beatContents = new ArrayList<>();

        for (int i = index; i < playables.size(); i++) {
            Playable playable = playables.get(i);
            if (playable.getCoordinates().isWithin(withinBeat)) {
                beatContents.add(playable);
                index++;
            } else {
                break;
            }
        }

       playListener.play(beatContents);
    }

}
