package com.dreamscale.htmflow.core.feeds.story.music;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Metronome {

    private MusicGeometryClock clock;

    private final List<Player> playerChain;

    public Metronome(MusicGeometryClock internalClock) {
        this.clock = internalClock;
        this.playerChain = new ArrayList<>();
    }

    void reset() {
        this.clock.reset();
    }

    public void tick() {
        MusicGeometryClock.Coords nextBeat = clock.tick();

        for (Player player : playerChain) {
            player.play(nextBeat);
        }

    }

    public void addPlayerToChain(Player player) {
        this.playerChain.add(player);
    }

    public int getBeats() {
        return clock.getBeats();
    }

    public MusicGeometryClock.Coords getCoords() {
        return clock.getCoordinates();
    }
}
