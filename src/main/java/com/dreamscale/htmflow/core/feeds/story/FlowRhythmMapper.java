package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.FlowStructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.FlowLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.FlowLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RelativeSequence;

import java.time.LocalDateTime;
import java.util.*;

public class FlowRhythmMapper {

    private final InnerGeometryClock internalClock;
    private InnerGeometryClock.Coords currentMoment;

    private Map<FlowStructureLevel, ContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<FlowStructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private Map<FlowLayerType, FlowLayer> layerMap = new HashMap<>();

    public FlowRhythmMapper(OuterGeometryClock.Coords storyCoordinates, ZoomLevel zoomLevel) {
        this.internalClock = new InnerGeometryClock(
                storyCoordinates.getClockTime(),
                storyCoordinates.panRight(zoomLevel).getClockTime());
    }

    private FlowLayer findOrCreateLayer(FlowLayerType layerType) {
        FlowLayer layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new FlowLayer(layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }

    public void addMovements(FlowLayerType layerType, List<MovementEvent> movementsToAdd) {
        for (MovementEvent movement : movementsToAdd) {
            addMovement(layerType, movement);
        }
    }

    public void addMovement(FlowLayerType layerType, MovementEvent movement) {
        FlowLayer layer = findOrCreateLayer(layerType);

        if (movement != null) {
            currentMoment = layer.addMovement(internalClock, movement);
        }
    }


    private RelativeSequence findOrCreateSequence(FlowStructureLevel structureLevel) {
        RelativeSequence relativeSequence = this.currentSequenceNumbers.get(structureLevel);
        if (relativeSequence == null) {
            relativeSequence = new RelativeSequence(1);
            this.currentSequenceNumbers.put(structureLevel, relativeSequence);
        }
        return relativeSequence;
    }


    public InnerGeometryClock.Coords getCurrentMoment() {
        return null;
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {

    }

    public Map<FlowLayerType, RelativeSequence> getLayerSequences() {
        Map<FlowLayerType, RelativeSequence> layerSequences = new HashMap<>();
        for (FlowLayer layer : this.layerMap.values()) {
            layerSequences.put(layer.getLayerType(), layer.getRelativeSequence());
        }
        return layerSequences;
    }


    public void initLayerSequencesFromPriorContext(Map<FlowLayerType, RelativeSequence> layerSequences) {
        for(FlowLayerType layerType : layerSequences.keySet()) {
            FlowLayer layer = findOrCreateLayer(layerType);
            int startingValue = layerSequences.get(layerType).getRelativeSequence() + 1;

            layer.initSequence(startingValue);
        }
    }

    public void finish() {
        for (FlowLayer layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public List<MovementEvent> getContextMovements() {
        FlowLayer contextLayer = layerMap.get(FlowLayerType.CONTEXT_CHANGES);

        return contextLayer.getMovements();
    }
}
