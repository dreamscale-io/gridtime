package com.dreamscale.htmflow.core.feeds.story.music;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MetronomePlayer {

    private final BeatSize beatSize;
    private MusicClock musicClock;

    private final List<Player> playerChain;
    private MusicClock.Beat currentBeat;


    public MetronomePlayer(MusicClock musicClock, BeatSize beatSize) {
        this.musicClock = musicClock;
        this.beatSize = beatSize;
        this.playerChain = new ArrayList<>();
    }

    void reset() {
        this.musicClock.reset();
    }

    public void tick() {
        this.currentBeat = musicClock.getCurrentBeat().toBeatSize(beatSize);

        for (Player player : playerChain) {
            player.play(currentBeat);
        }

        musicClock.next(beatSize);
    }

    public void addPlayerToChain(Player player) {
        this.playerChain.add(player);
    }

    public int getNumberOfTicks() {
        return musicClock.getBeats() / beatSize.getBeatCount();
    }

    public MusicClock.Beat getCurrentBeat() {
        return currentBeat;
    }
}
