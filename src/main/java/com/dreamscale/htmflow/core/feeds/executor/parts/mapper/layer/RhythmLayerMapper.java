package com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.ExecuteThing;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayer;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.RelativeSequence;
import com.dreamscale.htmflow.core.feeds.story.feature.context.MomentOfContext;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.Movement;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RhythmLayerMapper {

    private final RhythmLayerType layerType;
    private final FeatureFactory featureFactory;
    private final MusicGeometryClock internalClock;
    private final RhythmLayer layer;

    private Movement carriedOverLastMovement;

    private List<Movement> movementsToCarryUntilWithinWindow = new ArrayList<>();


    public RhythmLayerMapper(FeatureFactory featureFactory, MusicGeometryClock internalClock, RhythmLayerType layerType) {
        this.featureFactory = featureFactory;
        this.layerType = layerType;
        this.internalClock = internalClock;
        this.layer = featureFactory.createRhythmLayer(layerType);
    }

    public void addMovement(Movement movement) {

        movement.setCoordinates(internalClock.createCoords(movement.getMoment()));

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

}
