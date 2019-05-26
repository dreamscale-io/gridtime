package com.dreamscale.htmflow.core.feeds.story.music;

import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.*;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.AuthorsBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.FeelsBand;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.threshold.LearningFrictionBand;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGrid;

import java.util.List;

public class TileGridPlayer {

    private final MetronomePlayer metronomePlayer;
    private final Scene scene;
    private TileBuilder frameToPlay;

    public TileGridPlayer(TileGrid tileGrid) {

        this.metronomePlayer = tileGrid.createPlayer();
        this.scene = new Scene(tileGrid);
    }

    public void loadFrame(TileBuilder tileBuilder) {
        this.frameToPlay = tileBuilder;

        MomentOfContext initialContext = tileBuilder.getInitialContext();
        if (initialContext != null) {
            scene.changeProjectContext(initialContext.getProjectContext());
            scene.changeTaskContext(initialContext.getTaskContext());
            scene.changeIntentionContext(initialContext.getIntentionContext());
        }

        metronomePlayer.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FEELS), new FeelsListener()));
        metronomePlayer.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_WTF), new WTFListener()));
        metronomePlayer.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.FRICTION_LEARNING), new LearningListener()));
        metronomePlayer.addPlayerToChain(new BandPlayer(frameToPlay.getBandLayer(BandLayerType.AUTHORS), new AuthorsListener()));

        metronomePlayer.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.LOCATION_CHANGES), new LocationListener()));
        metronomePlayer.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.CONTEXT_CHANGES), new ContextChangeListener()));
        metronomePlayer.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.EXECUTION_ACTIVITY), new ExecutionActivityListener()));
        metronomePlayer.addPlayerToChain(new RhythmPlayer(frameToPlay.getRhythmLayer(RhythmLayerType.CIRCLE_MESSAGE_EVENTS), new CircleMessageActivityListener()));
    }

    public void play() {

        int ticksToPlay = metronomePlayer.getNumberOfTicks();

        for (int i = 0; i < ticksToPlay; i++) {
            scene.panForwardTime();
            metronomePlayer.tick();

            scene.snapshot(metronomePlayer.getCurrentClockBeat());
        }

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
                return Math.floorDiv(total, playables.size());
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
            //I want to generate an authors band here,
            // that is more legit aggregation per person, or per pair of persons

            if (playables.size() > 0) {
                AuthorsBand authorsBand = (AuthorsBand) playables.get(playables.size() - 1);

                if (authorsBand.getAuthorCount() > 1) {
                    scene.setIsPairing(true);
                } else {
                    scene.setIsPairing(false);
                }
                scene.changeActiveAuthors(authorsBand.getAuthors());
            }
        }
    }

    private class LocationListener implements PlayListener {


        @Override
        public void play(List<Playable> playables) {

            BoxAndBridgeActivity structure = frameToPlay.getSpatialStructure();

            for (Playable playable : playables) {
                if (playable instanceof MoveToBox) {
                    scene.pushActiveBox(((MoveToBox) playable).getBox());
                }

                if (playable instanceof MoveToLocation) {
                    MoveToLocation moveToLocation = (MoveToLocation) playable;
                    scene.pushActiveLocation(moveToLocation.getLocation());
                    scene.pushActiveTraversal(moveToLocation.getTraversal());

                    ThoughtBubble bubble = structure.findBubbleContaining(moveToLocation.getLocation());
                    ThoughtBubble.RingLocation ringLocation = bubble.findRingLocation(moveToLocation.getLocation());
                    ThoughtBubble.Link ringLink = bubble.findRingLink(moveToLocation.getTraversal());

                    scene.pushActiveBubble(bubble);
                    scene.pushActiveRingLocation(ringLocation);
                    scene.pushActiveRingLink(ringLink);
                }

                if (playable instanceof MoveAcrossBridge) {
                    Bridge bridge = ((MoveAcrossBridge) playable).getBridge();
                    scene.pushActiveBridge(bridge);

                }

            }
        }
    }

    private class ContextChangeListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            for (Playable playable : playables) {
                ChangeContext changeContext = (ChangeContext) playable;
                if (changeContext.getStructureLevel().equals(StructureLevel.PROJECT)) {
                    scene.changeProjectContext(changeContext.getChangingContext());
                }
                if (changeContext.getStructureLevel().equals(StructureLevel.TASK)) {
                    scene.changeTaskContext(changeContext.getChangingContext());
                }
                if (changeContext.getStructureLevel().equals(StructureLevel.INTENTION)) {
                    scene.changeIntentionContext(changeContext.getChangingContext());
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
            }

        }
    }

    private class CircleMessageActivityListener implements PlayListener {

        @Override
        public void play(List<Playable> playables) {
            //what do I want to do with this?  Anything?

            for (Playable playable : playables) {
                PostCircleMessage circleMessage = (PostCircleMessage) playable;

                scene.pushCircleMessageEvent(circleMessage);
            }

        }
    }

}
