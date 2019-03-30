package com.dreamscale.ideaflow.core.feeds.story;

import com.dreamscale.ideaflow.core.feeds.story.see.MusicalGeometryClock;
import com.dreamscale.ideaflow.core.feeds.clock.GeometryClock;
import com.dreamscale.ideaflow.core.feeds.common.ZoomLevel;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowContextBeginningEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.context.IdeaFlowStructureLevel;
import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.IdeaFlowLayer;
import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.IdeaFlowLayerType;
import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.IdeaFlowMovementEvent;
import com.dreamscale.ideaflow.core.feeds.story.feature.sequence.RelativeSequence;

import java.time.LocalDateTime;
import java.util.*;

public class IdeaFlowSequenceMapper {

    private final MusicalGeometryClock internalClock;
    private MusicalGeometryClock.Coords currentMoment;

    private Map<IdeaFlowStructureLevel, IdeaFlowContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<IdeaFlowStructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private Map<IdeaFlowLayerType, IdeaFlowLayer> layerMap = new HashMap<>();

    public IdeaFlowSequenceMapper(GeometryClock.Coords storyCoordinates, ZoomLevel zoomLevel) {
        this.internalClock = new MusicalGeometryClock(
                storyCoordinates.getClockTime(),
                storyCoordinates.panRight(zoomLevel).getClockTime());
    }

    private IdeaFlowLayer findOrCreateLayer(IdeaFlowLayerType layerType) {
        IdeaFlowLayer layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new IdeaFlowLayer(layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }

    public void addMovements(IdeaFlowLayerType layerType, List<IdeaFlowMovementEvent> movementsToAdd) {
        for (IdeaFlowMovementEvent movement : movementsToAdd) {
            addMovement(layerType, movement);
        }
    }

    public void addMovement(IdeaFlowLayerType layerType, IdeaFlowMovementEvent movement) {
        IdeaFlowLayer layer = findOrCreateLayer(layerType);

        if (movement != null) {
            currentMoment = layer.addMovement(internalClock, movement);
        }
    }


    private RelativeSequence findOrCreateSequence(IdeaFlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }


    public MusicalGeometryClock.Coords getCurrentMoment() {
        return null;
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

    }

    public Map<IdeaFlowLayerType, RelativeSequence> getLayerSequences() {
        Map<IdeaFlowLayerType, RelativeSequence> layerSequences = new HashMap<>();
        for (IdeaFlowLayer layer : this.layerMap.values()) {
            layerSequences.put(layer.getLayerType(), layer.getRelativeSequence());
        }
        return layerSequences;
    }


    public void initLayerSequencesFromPriorContext(Map<IdeaFlowLayerType, RelativeSequence> layerSequences) {
        for(IdeaFlowLayerType layerType : layerSequences.keySet()) {
            IdeaFlowLayer layer = findOrCreateLayer(layerType);
            int startingValue = layerSequences.get(layerType).getRelativeSequence() + 1;

            layer.initSequence(startingValue);
        }
    }

    public void finish() {
        for (IdeaFlowLayer layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public List<IdeaFlowMovementEvent> getContextMovements() {
        IdeaFlowLayer contextLayer = layerMap.get(IdeaFlowLayerType.CONTEXT_CHANGES);

        return contextLayer.getMovements();
    }
}
