package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.CircleMessageContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.RhythmLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.ExecutionContext;
import com.dreamscale.htmflow.core.feeds.story.feature.sequence.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInPlace;
import com.dreamscale.htmflow.core.feeds.story.mapper.layer.RhythmLayerMapper;

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

        ModificationEvent modificationEvent = new ModificationEvent(moment, location, modificationCount);
        modificationLayer.addMovement(internalClock, new Movement(moment, modificationEvent));

    }

    public void execute(LocalDateTime moment, ExecutionContext executionContext) {

        RhythmLayerMapper executionLayer = layerMap.get(RhythmLayerType.EXECUTION_ACTIVITY);

        if (executionContext.getDuration().getSeconds() > 60) {
            ExecutionStartEvent executionStartEvent = new ExecutionStartEvent(moment, executionContext);
            Movement startMovement = new Movement(moment, executionStartEvent);
            executionLayer.addMovement(internalClock, startMovement);

            LocalDateTime endTime = moment.plusSeconds(executionContext.getDuration().getSeconds());
            ExecutionEndEvent executionEndEvent = new ExecutionEndEvent(endTime, executionContext);
            Movement endMovement = new Movement(endTime, executionEndEvent);
            executionLayer.addMovementLater(endMovement);

        } else {
            Movement movement = new Movement(moment, new ExecutionEvent(moment, executionContext));
            executionLayer.addMovement(internalClock, movement);
        }

    }

    public void postCircleMessage(LocalDateTime moment, CircleMessageContext messageContext) {

        RhythmLayerMapper circleMessageLayer = layerMap.get(RhythmLayerType.CIRCLE_MESSAGE_EVENTS);

        circleMessageLayer.addMovement(internalClock, new Movement(moment, messageContext));

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

            List<Movement> carryOverMovements = layer.getMovementsToCarryUntilWithinWindow();
            subContext.addCarryOverMovements(layer.getLayerType(), carryOverMovements);
        }

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Set<RhythmLayerType> layerTypes = subContext.getLayerTypes();

        for (RhythmLayerType layerType : layerTypes) {
            RhythmLayerMapper layer = findOrCreateLayer(layerType);

            layer.initContext(subContext.getLastMovement(layerType));

            placeCarryOverMovements(subContext, layerType);
        }

    }

    private void placeCarryOverMovements(CarryOverSubContext subContext, RhythmLayerType layerType) {
        List<? extends Movement> carryOverMovements = subContext.getCarryOverMovements(layerType);
        if (carryOverMovements != null) {
            for (Movement movement : carryOverMovements) {

                LocalDateTime position = movement.getMoment();

                if ((from.isBefore(position) || from.isEqual(position)) && to.isAfter(position)) {
                    addMovement(layerType, movement);

                } else {
                    RhythmLayerMapper layerMapper = layerMap.get(layerType);
                    layerMapper.addMovementLater(movement);
                }
            }
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

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {
        return this.layerMap.get(rhythmLayerType).getLastMovement();
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
            String key = layerType.name() + ".last.movement";
            subContext.addKeyValue(key, movement);
        }

        Movement getLastMovement(RhythmLayerType layerType) {
            String key = layerType.name() + ".last.movement";
            return (Movement) subContext.getValue(key);
        }

        public Set<RhythmLayerType> getLayerTypes() {
            Set<String> keys = subContext.keySet();

            Set<RhythmLayerType> layerTypes = new LinkedHashSet<>();
            for (String key : keys) {

                String layerTypeName = parseLayerTypeFromKey(key);
                layerTypes.add(RhythmLayerType.valueOf(layerTypeName));
            }

            return layerTypes;
        }

        private String parseLayerTypeFromKey(String key) {
            return key.substring(0, key.indexOf("."));
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }

        public void addCarryOverMovements(RhythmLayerType layerType, List<Movement> carryOverMovements) {
            String key = layerType.name() + ".carry.over.movements";
            subContext.addKeyList(key, carryOverMovements);
        }

        public List<? extends Movement> getCarryOverMovements(RhythmLayerType layerType) {
            String key = layerType.name() + ".carry.over.movements";
            return (List<? extends Movement>) subContext.getKeyList(key);
        }
    }


}
