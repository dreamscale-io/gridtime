package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class MetronomePlayer {

    private MusicClock musicClock;

    private final List<Player> playerChain;
    private ClockBeat currentClockBeat;


    public MetronomePlayer(MusicClock musicClock) {
        this.musicClock = musicClock;
        this.playerChain = new ArrayList<>();
    }

    void reset() {
        this.musicClock.reset();
    }

    public void tick() {
        this.currentClockBeat = musicClock.getCurrentBeat();

        for (Player player : playerChain) {
            player.play(currentClockBeat);
        }

        musicClock.next();
    }

    public void addPlayerToChain(Player player) {
        this.playerChain.add(player);
    }

    public int getNumberOfTicks() {
        return musicClock.getBeats();
    }

    public ClockBeat getCurrentClockBeat() {
        return currentClockBeat;
    }
}
