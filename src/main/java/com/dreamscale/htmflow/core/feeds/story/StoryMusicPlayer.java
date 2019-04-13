package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.FeelsBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.LearningFrictionBand;
import com.dreamscale.htmflow.core.feeds.story.music.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StoryMusicPlayer {

    private final Metronome metronome;
    private final Scene scene;
    private StoryFrame frameToPlay;
    private List<Snapshot> snapshots;

    public StoryMusicPlayer(LocalDateTime from, LocalDateTime to) {
        this.metronome = new Metronome(from, to);
        this.scene = new Scene();
    }

    public void loadFrame(StoryFrame storyFrame) {
        this.frameToPlay = storyFrame;

        metronome.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FEELS), new FeelsListener()));
        metronome.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_WTF), new WTFListener()));
        metronome.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_LEARNING), new LearningListener()));
        metronome.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.PAIRING_AUTHORS), new AuthorsListener()));

        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES), new LocationListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES), new ContextChangeListener()));
        metronome.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.EXECUTION_ACTIVITY), new ExecutionActivityListener()));

    }

    public void play() {
        List<Snapshot> snapshots = new ArrayList<>();

        int beatsToPlay = metronome.getBeats();

        for (int i = 0; i < beatsToPlay; i++) {
            scene.panForwardTime();
            metronome.tick();

            snapshots.add(scene.snapshot(metronome.getCoords()));
        }

        this.snapshots = snapshots;
    }

    public List<Snapshot> getSnapshots() {
        return snapshots;
    }

    private class FeelsListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            scene.updateFeels(aggregateFeels(playables));
        }

        int aggregateFeels(List<Playable> playables) {
            if (playables.size() > 0) {
                int total = 0;
                for (Playable playable : playables) {
                    total += ((FeelsBand) playable).getFeels();
                }
                return total / playables.size();
            } else {
                return 0;
            }
        }
    }

    private class WTFListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                scene.updateWTFFriction(true);
            } else {
                scene.updateWTFFriction(false);
            }
        }
    }

    private class LearningListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                LearningFrictionBand learningBand = (LearningFrictionBand) playables.get(playables.size() - 1);
                if (!learningBand.isOverModifyThreshold()) {
                    scene.updateLearningFriction(true);
                } else {
                    scene.updateLearningFriction(false);
                }
            }
        }
    }

    private class AuthorsListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            if (playables.size() > 0) {
                scene.setIsPairing(true);
            } else {
                scene.setIsPairing(false);
            }
        }
    }

    private class LocationListener implements PlayListener {


        @Override
        public void play(List<Playable> playables) {

            BoxAndBridgeStructure structure = frameToPlay.getThoughtStructure();

            for (Playable playable : playables) {
                if (playable instanceof MoveToBox) {
                    scene.pushActiveBox(((MoveToBox) playable).getBox());
                }

                if (playable instanceof MoveToLocation) {
                    MoveToLocation moveToLocation = (MoveToLocation) playable;
                    scene.pushActiveLocation(moveToLocation.getLocation());
                    scene.pushActiveTraversal(moveToLocation.getTraversal());

                    Box box = structure.findBoxContaining(moveToLocation.getLocation());
                    ThoughtBubble bubble = box.findBubbleContainingLocation(moveToLocation.getLocation());
                    RadialStructure.RingLocation ringLocation = bubble.findRingLocation(moveToLocation.getLocation());
                    RadialStructure.Link ringLink = bubble.findRingTraversal(moveToLocation.getTraversal());

                    scene.pushUrisInScene(box.getUri(), bubble.getUri(), ringLocation.getUri(), ringLink.getUri());
                }

                if (playable instanceof MoveAcrossBridge) {
                    Bridge bridge = ((MoveAcrossBridge) playable).getBridge();
                    scene.pushActiveBridge(bridge);

                    Box fromBox = bridge.getFromBox();
                    Box toBox = bridge.getToBox();

                    ThoughtBubble fromBubble = fromBox.findBubbleContainingLocation(bridge.getFromLocation());
                    ThoughtBubble toBubble = toBox.findBubbleContainingLocation(bridge.getToLocation());

                    BridgeToBubble fromBridgeToBubble = fromBubble.findBridgeToBubbleLink(bridge);
                    BridgeToBubble toBridgeToBubble = toBubble.findBridgeToBubbleLink(bridge);

                    scene.pushUrisInScene(fromBridgeToBubble.getUri(), toBridgeToBubble.getUri());
                }

            }
        }
    }

    private class ContextChangeListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            for (Playable playable : playables) {
                ContextChangeEvent contextChangeEvent = (ContextChangeEvent) playable;
                if (contextChangeEvent.getEventType().equals(ContextChangeEvent.Type.BEGINNING)) {
                    if (contextChangeEvent.getStructureLevel().equals(ContextStructureLevel.PROJECT)) {
                        scene.changeProjectContext(contextChangeEvent.getContext());
                    }
                    if (contextChangeEvent.getStructureLevel().equals(ContextStructureLevel.TASK)) {
                        scene.changeTaskContext(contextChangeEvent.getContext());
                    }
                    if (contextChangeEvent.getStructureLevel().equals(ContextStructureLevel.INTENTION)) {
                        scene.changeIntentionContext(contextChangeEvent.getContext());
                    }
                }
            }

        }
    }

    private class ExecutionActivityListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            //what do I want to do with this?  Anything?

            for (Playable playable : playables) {
                ExecuteThing executeEvent = (ExecuteThing) playable;

                scene.pushExecuteEvent(executeEvent);
                scene.pushUrisInScene(executeEvent.getUri());
            }

        }
    }

}
