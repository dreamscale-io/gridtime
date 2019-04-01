package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.ExecutionContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInFocus;

import java.time.LocalDateTime;
import java.util.*;

public class FlowRhythmMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final InnerGeometryClock internalClock;

    private InnerGeometryClock.Coords currentMoment;

    private Map<RhythmLayerType, RhythmLayer> layerMap = new HashMap<>();

    public FlowRhythmMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;

        this.internalClock = new InnerGeometryClock(from, to);
    }


    private RhythmLayer findOrCreateLayer(RhythmLayerType layerType) {
        RhythmLayer layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new RhythmLayer(layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }

    public void addMovements(RhythmLayerType layerType, List<Movement> movementsToAdd) {
        for (Movement movement : movementsToAdd) {
            addMovement(layerType, movement);
        }
    }

    public void addMovement(RhythmLayerType layerType, Movement movement) {
        RhythmLayer layer = findOrCreateLayer(layerType);

        if (movement != null) {
            currentMoment = layer.addMovement(internalClock, movement);
        }
    }

    public InnerGeometryClock.Coords getCurrentMoment() {
        return currentMoment;
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {
        RhythmLayer modificationLayer = findOrCreateLayer(RhythmLayerType.MODIFICATION);

        LocationInFocus location = getLastLocation();

        ModificationEvent modificationEvent = new ModificationEvent(moment, location, modificationCount);
        modificationLayer.addMovement(internalClock, modificationEvent);

    }

    public void executeFromCurrentLocation(LocalDateTime moment, ExecutionContext executionContext) {
        LocationInFocus location = getLastLocation();

        RhythmLayer executionLayer = layerMap.get(RhythmLayerType.EXECUTION);

        ExecutionEvent executionEvent = new ExecutionEvent(moment, location, executionContext);
        executionLayer.addMovement(internalClock, executionEvent);

    }

    private LocationInFocus getLastLocation() {
        RhythmLayer locationLayer = findOrCreateLayer(RhythmLayerType.LOCATION_CHANGES);
        Movement movement = locationLayer.getLastMovement();

        LocationInFocus location = null;
        if (movement != null && movement.getReference() instanceof LocationInFocus) {
            location = (LocationInFocus) movement.getReference();
        }
        return location;
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverContext carryOverContext = new CarryOverContext();

        for (RhythmLayer layer : this.layerMap.values()) {
            Movement lastMovement = layer.getLastMovement();
            carryOverContext.addLassMovement(layer.getLayerType(), lastMovement);
        }
        return carryOverContext;
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {
        Set<RhythmLayerType> layerTypes = carryOverContext.getLayerTypes();


        for (RhythmLayerType layerType : layerTypes) {
            RhythmLayer layer = findOrCreateLayer(layerType);

            layer.initContext(carryOverContext.getLastMovement(layerType));
        }

    }

    public void finish() {
        for (RhythmLayer layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public List<Movement> getMovements(RhythmLayerType layerType) {
        return layerMap.get(layerType).getMovements();
    }

    public static class CarryOverContext {
        Map<RhythmLayerType, Movement> lastMovements = new HashMap<>();

        void addLassMovement(RhythmLayerType layerType, Movement movement) {
            lastMovements.put(layerType, movement);
        }

        Movement getLastMovement(RhythmLayerType layerType) {
            return lastMovements.get(layerType);
        }

        public Set<RhythmLayerType> getLayerTypes() {
            return lastMovements.keySet();
        }
    }


}
