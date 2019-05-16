package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.music.MusicClock;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;

import java.util.ArrayList;
import java.util.List;

public class RhythmLayerMapper {

    private final RhythmLayerType layerType;
    private final FeatureFactory featureFactory;
    private final MusicClock musicClock;
    private final RhythmLayer layer;

    private Movement carriedOverLastMovement;

    private List<Movement> movementsToCarryUntilWithinWindow = new ArrayList<>();


    public RhythmLayerMapper(FeatureFactory featureFactory, MusicClock musicClock, RhythmLayerType layerType) {
        this.featureFactory = featureFactory;
        this.layerType = layerType;
        this.musicClock = musicClock;
        this.layer = featureFactory.createRhythmLayer(layerType);
    }

    public void addMovement(Movement movement) {

        movement.setCoordinates(musicClock.createBeat(movement.getMoment()));

        layer.add(movement);

    }

    public void addMovementLater(Movement movement) {
        movementsToCarryUntilWithinWindow.add(movement);
    }

    public List<Movement> getMovementsToCarryUntilWithinWindow() {
        return movementsToCarryUntilWithinWindow;
    }


    public RhythmLayerType getLayerType() {
        return layerType;
    }

    public RhythmLayer getRhythmLayer() {
        return layer;
    }

    public void initContext(Movement lastMovement) {
        this.carriedOverLastMovement = lastMovement;
    }


    public void finish() {
        layer.repairSortingAndSequenceNumbers();
    }

    public Movement getLastMovement() {
        Movement lastMovement = layer.getLastMovement();

        if (lastMovement == null) {
            lastMovement = carriedOverLastMovement;
        }

        return lastMovement;
    }

    public boolean isEmpty() {
        return layer.getMovements().size() == 0;
    }
}
