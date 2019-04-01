package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RhythmLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.ExecutionContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInPlace;

import java.time.LocalDateTime;
import java.util.*;

public class FlowRhythmMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final InnerGeometryClock internalClock;

    private InnerGeometryClock.Coords currentMoment;

    private Map<RhythmLayerType, RhythmLayerMapper> layerMap = new HashMap<>();

    public FlowRhythmMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;

        this.internalClock = new InnerGeometryClock(from, to);
    }


    private RhythmLayerMapper findOrCreateLayer(RhythmLayerType layerType) {
        RhythmLayerMapper layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new RhythmLayerMapper(layerType);
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
        RhythmLayerMapper layer = findOrCreateLayer(layerType);

        if (movement != null) {
            currentMoment = layer.addMovement(internalClock, movement);
        }
    }

    public InnerGeometryClock.Coords getCurrentMoment() {
        return currentMoment;
    }

    public void modifyCurrentLocation(LocalDateTime moment, int modificationCount) {
        RhythmLayerMapper modificationLayer = findOrCreateLayer(RhythmLayerType.MODIFICATION_ACTIVITY);

        LocationInPlace location = getLastLocation();

        ModificationEvent modificationEvent = new ModificationEvent(moment, location, new ModificationContext(modificationCount));
        modificationLayer.addMovement(internalClock, modificationEvent);

    }

    public void executeFromCurrentLocation(LocalDateTime moment, ExecutionContext executionContext) {
        LocationInPlace location = getLastLocation();

        RhythmLayerMapper executionLayer = layerMap.get(RhythmLayerType.EXECUTION_ACTIVITY);

        ExecutionEvent executionEvent = new ExecutionEvent(moment, location, executionContext);
        executionLayer.addMovement(internalClock, executionEvent);

    }

    private LocationInPlace getLastLocation() {
        RhythmLayerMapper locationLayer = findOrCreateLayer(RhythmLayerType.LOCATION_CHANGES);
        Movement movement = locationLayer.getLastMovement();

        LocationInPlace location = null;
        if (movement != null && movement.getReference() instanceof LocationInPlace) {
            location = (LocationInPlace) movement.getReference();
        }
        return location;
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();

        for (RhythmLayerMapper layer : this.layerMap.values()) {
            Movement lastMovement = layer.getLastMovement();
            subContext.addLassMovement(layer.getLayerType(), lastMovement);
        }

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Set<RhythmLayerType> layerTypes = subContext.getLayerTypes();


        for (RhythmLayerType layerType : layerTypes) {
            RhythmLayerMapper layer = findOrCreateLayer(layerType);

            layer.initContext(subContext.getLastMovement(layerType));
        }

    }

    public void finish() {
        for (RhythmLayerMapper layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public RhythmLayer getRhythmLayer(RhythmLayerType layerType) {
        return new RhythmLayer(from, to, layerMap.get(layerType).getMovements());
    }

    public Set<RhythmLayerType> getRhythmLayerTypes() {
        return layerMap.keySet();
    }

    public static class CarryOverSubContext {

        private static final String SUBCONTEXT_NAME = "[FlowRhythmMapper]";

        private final CarryOverContext subContext;

        public CarryOverSubContext() {
             subContext = new CarryOverContext(SUBCONTEXT_NAME);
        }

        public CarryOverSubContext(CarryOverContext mainContext) {
            subContext = mainContext.getSubContext(SUBCONTEXT_NAME);
        }

        void addLassMovement(RhythmLayerType layerType, Movement movement) {
            subContext.addKeyValue(layerType.name(), movement);
        }

        Movement getLastMovement(RhythmLayerType layerType) {
            return (Movement) subContext.getValue(layerType.name());
        }

        public Set<RhythmLayerType> getLayerTypes() {
            Set<String> keys = subContext.keySet();

            Set<RhythmLayerType> layerTypes = new LinkedHashSet<>();
            for (String key : keys) {
                layerTypes.add(RhythmLayerType.valueOf(key));
            }

            return layerTypes;
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }
    }


}
