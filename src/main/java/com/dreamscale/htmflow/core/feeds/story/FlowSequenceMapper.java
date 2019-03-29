package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextBeginningEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextChangeEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.Layer;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.LayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.MovementEvent;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RelativeSequence;

import java.time.LocalDateTime;
import java.util.*;

public class FlowSequenceMapper {

    private final InnerGeometryClock internalClock;
    private InnerGeometryClock.Coords currentMoment;

    private Map<StructureLevel, ContextBeginningEvent> currentContextMap = new HashMap<>();
    private Map<StructureLevel, RelativeSequence> currentSequenceNumbers = new HashMap<>();

    private Map<LayerType, Layer> layerMap = new HashMap<>();

    public FlowSequenceMapper(OuterGeometryClock.Coords storyCoordinates, ZoomLevel zoomLevel) {
        this.internalClock = new InnerGeometryClock(
                storyCoordinates.getClockTime(),
                storyCoordinates.panRight(zoomLevel).getClockTime());
    }

    private Layer findOrCreateLayer(LayerType layerType) {
        Layer layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new Layer(layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }

    public void addMovements(LayerType layerType, List<MovementEvent> movementsToAdd) {
        for (MovementEvent movement : movementsToAdd) {
            addMovement(layerType, movement);
        }
    }

    public void addMovement(LayerType layerType, MovementEvent movement) {
        Layer layer = findOrCreateLayer(layerType);

        if (movement != null) {
            currentMoment = layer.addMovement(internalClock, movement);
        }
    }


    private RelativeSequence findOrCreateSequence(StructureLevel structureLevel) {
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

    public void modifyActiveFlow(LocalDateTime moment, int modificationCount) {

    }

    public Map<LayerType, RelativeSequence> getLayerSequences() {
        Map<LayerType, RelativeSequence> layerSequences = new HashMap<>();
        for (Layer layer : this.layerMap.values()) {
            layerSequences.put(layer.getLayerType(), layer.getRelativeSequence());
        }
        return layerSequences;
    }


    public void initLayerSequencesFromPriorContext(Map<LayerType, RelativeSequence> layerSequences) {
        for(LayerType layerType : layerSequences.keySet()) {
            Layer layer = findOrCreateLayer(layerType);
            int startingValue = layerSequences.get(layerType).getRelativeSequence() + 1;

            layer.initSequence(startingValue);
        }
    }

    public void repairSortingAndSequenceNumbers() {
        for (Layer layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public List<MovementEvent> getContextMovements() {
        Layer contextLayer = layerMap.get(LayerType.CONTEXT_CHANGES);

        return contextLayer.getMovements();
    }
}
