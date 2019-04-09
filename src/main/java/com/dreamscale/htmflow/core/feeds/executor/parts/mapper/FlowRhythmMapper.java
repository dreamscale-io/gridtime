package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.context.ContextSummary;
import com.dreamscale.htmflow.core.feeds.story.feature.details.IdeaDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.RhythmLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer.RhythmLayerMapper;

import java.time.LocalDateTime;
import java.util.*;

public class FlowRhythmMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final InnerGeometryClock internalClock;

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

    public void addMovements(RhythmLayerType layerType, ContextSummary context, List<Movement> movementsToAdd) {
        for (Movement movement : movementsToAdd) {
            addMovement(layerType, context,  movement);
        }
    }

    public void addMovement(RhythmLayerType layerType, ContextSummary context, Movement movement) {
        RhythmLayerMapper layer = findOrCreateLayer(layerType);

        if (movement != null) {
            layer.addMovement(internalClock, context, movement);
        }
    }

    public void modifyCurrentLocation(LocalDateTime moment, ContextSummary context, int modificationCount) {
        RhythmLayerMapper modificationLayer = findOrCreateLayer(RhythmLayerType.MODIFICATION_ACTIVITY);

        LocationInBox location = getLastLocation();

        modificationLayer.addMovement(internalClock, context, new ModifyLocation(moment, location, modificationCount));

    }

    public void executeThing(LocalDateTime moment, ContextSummary context, ExecutionDetails executionDetails) {

        RhythmLayerMapper executionLayer = layerMap.get(RhythmLayerType.EXECUTION_ACTIVITY);

        if (executionDetails.getDuration().getSeconds() > 60) {
            ExecuteThing startExecution = new ExecuteThing(moment, executionDetails, ExecuteThing.EventType.START_LONG_EXECUTION);
            executionLayer.addMovement(internalClock, context, startExecution);

            LocalDateTime endTime = moment.plusSeconds(executionDetails.getDuration().getSeconds());
            ExecuteThing endExecution = new ExecuteThing(endTime, executionDetails, ExecuteThing.EventType.END_LONG_EXECUTION);
            executionLayer.addMovementLater(endExecution);

        } else {
            Movement executeThing = new ExecuteThing(moment, executionDetails, ExecuteThing.EventType.EXECUTE_EVENT);
            executionLayer.addMovement(internalClock, context, executeThing);
        }

    }

    public void shareMessage(LocalDateTime moment, ContextSummary context, IdeaDetails ideaDetails) {

        RhythmLayerMapper circleMessageLayer = layerMap.get(RhythmLayerType.CIRCLE_MESSAGE_EVENTS);
        circleMessageLayer.addMovement(internalClock, context, new ShareMessage(moment, ideaDetails));

    }


    private LocationInBox getLastLocation() {
        RhythmLayerMapper locationLayer = findOrCreateLayer(RhythmLayerType.LOCATION_CHANGES);
        Movement movement = locationLayer.getLastMovement();

        LocationInBox location = null;
        if (movement instanceof MoveToLocation) {
            location = ((MoveToLocation) movement).getLocation();
        }
        return location;
    }

    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();

        for (RhythmLayerMapper layer : this.layerMap.values()) {
            Movement lastMovement = layer.getLastMovement();
            subContext.addLastMovement(layer.getLayerType(), lastMovement);

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
                    //TODO need some way to populate context for this... can we fix in the repair?
                    addMovement(layerType, null, movement);

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
        return new RhythmLayer(layerType, from, to, layerMap.get(layerType).getMovements());
    }

    public Set<RhythmLayerType> getRhythmLayerTypes() {
        return layerMap.keySet();
    }

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {
        return this.layerMap.get(rhythmLayerType).getLastMovement();
    }

    public List<RhythmLayer> getRhythmLayers() {
        List<RhythmLayer> rhythmLayers = new ArrayList<>();

        for (RhythmLayerType layerType : layerMap.keySet()) {
            rhythmLayers.add(getRhythmLayer(layerType));
        }
        return rhythmLayers;
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

        void addLastMovement(RhythmLayerType layerType, Movement movement) {
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
