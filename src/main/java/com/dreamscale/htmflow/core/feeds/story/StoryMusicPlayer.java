package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.FeelsBand;
import com.dreamscale.htmflow.core.feeds.story.music.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoryMusicPlayer {

    private final Metronome metronome;
    private final Scene scene;
    private StoryFrame frameToPlay;

    public StoryMusicPlayer(LocalDateTime from, LocalDateTime to) {
        this.metronome = new Metronome(from, to);
        this.scene = new Scene();
    }

    public void loadFrame(StoryFrame storyFrame) {
        this.frameToPlay = storyFrame;

        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getBandLayer(BandLayerType.FEELS), new FeelsListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_WTF), new WTFListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_LEARNING), new LearningListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getBandLayer(BandLayerType.PAIRING_AUTHORS), new AuthorsListener()));

        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES), new LocationListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES), new ContextListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.EXECUTION_ACTIVITY), new ExecutionActivityListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.CIRCLE_MESSAGE_EVENTS), new CircleMessageListener()));

    }

    public List<Snapshot> play() {
        List<Snapshot> snapshots = new ArrayList<>();

        int beatsToPlay = metronome.getBeats();

        for (int i = 0; i < beatsToPlay; i++) {
            MusicGeometryClock.Coords tickCoords = metronome.tick();
            snapshots.add(scene.snapshot(tickCoords));
        }

        return snapshots;
    }

    private class FeelsListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class WTFListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class LearningListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class AuthorsListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class LocationListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class ContextListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class ExecutionActivityListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }

    private class CircleMessageListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                FeelsBand feelsBand = (FeelsBand)playables.get(playables.size() - 1);
                scene.updateFeels(feelsBand.getFeels());
            }
        }
    }
}
