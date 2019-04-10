package com.dreamscale.htmflow.core.feeds.story.music;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Metronome {

    private MusicGeometryClock clock;

    private final List<RhythmPlayer> playerChain;

    public Metronome(LocalDateTime from, LocalDateTime to) {
        this.clock = new MusicGeometryClock(from, to);
        this.playerChain = new ArrayList<>();
    }

    void reset() {
        this.clock.reset();
    }

    public MusicGeometryClock.Coords tick() {
        MusicGeometryClock.Coords nextBeat = clock.tick();

        for (RhythmPlayer player : playerChain) {
            player.tick(nextBeat);
        }

        return nextBeat;
    }

    public void addPlayerToChain(RhythmPlayer rhythmPlayer) {
        this.playerChain.add(rhythmPlayer);
    }

    public int getBeats() {
        return clock.getBeats();
    }
}
