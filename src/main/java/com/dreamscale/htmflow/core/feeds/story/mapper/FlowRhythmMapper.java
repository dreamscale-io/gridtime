package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.story.mapper.layer.RhythmLayerMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ExecutionDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.details.MessageDetails;
import com.dreamscale.htmflow.core.feeds.story.feature.movement.*;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;

import java.time.LocalDateTime;
import java.util.*;

public class FlowRhythmMapper {

    private final MusicClock musicClock;
    private final FeatureFactory featureFactory;

    private Map<RhythmLayerType, RhythmLayerMapper> layerMap = new HashMap<>();

    public FlowRhythmMapper(FeatureFactory featureFactory, MusicClock musicClock) {
        this.featureFactory = featureFactory;
        this.musicClock = musicClock;
    }


    private RhythmLayerMapper findOrCreateLayer(RhythmLayerType layerType) {
        RhythmLayerMapper layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new RhythmLayerMapper(featureFactory, musicClock, layerType);
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
            layer.addMovement(movement);
        }
    }

    public void executeThing(LocalDateTime moment, ExecutionDetails executionDetails) {

        RhythmLayerMapper executionLayer = findOrCreateLayer(RhythmLayerType.EXECUTION_ACTIVITY);

        if (executionDetails.getDuration().getSeconds() > 60) {
            ExecuteThing startExecution = featureFactory.createExecuteThing(moment, executionDetails, ExecuteThing.EventType.START_LONG_EXECUTION);
            executionLayer.addMovement(startExecution);

            LocalDateTime endTime = moment.plusSeconds(executionDetails.getDuration().getSeconds());
            ExecuteThing endExecution = featureFactory.createExecuteThing(endTime, executionDetails, ExecuteThing.EventType.END_LONG_EXECUTION);
            executionLayer.addMovementLater(endExecution);

        } else {
            Movement executeThing = featureFactory.createExecuteThing(moment, executionDetails, ExecuteThing.EventType.EXECUTE_EVENT);
            executionLayer.addMovement(executeThing);
        }
    }

    public void shareMessage(LocalDateTime moment, MessageDetails messageDetails) {

        RhythmLayerMapper circleMessageLayer = layerMap.get(RhythmLayerType.CIRCLE_MESSAGE_EVENTS);

        PostCircleMessage postCircleMessage = featureFactory.createPostCircleMessage(moment, messageDetails);

        circleMessageLayer.addMovement(postCircleMessage);

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

                LocalDateTime from = musicClock.getFromClockTime();
                LocalDateTime to = musicClock.getToClockTime();

                if ((from.isBefore(position) || from.isEqual(position)) && to.isAfter(position)) {
                    //TODO need some way to populate context for this... can we fix in the repair?
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
            layer.finish();
        }
    }

    public RhythmLayer getRhythmLayer(RhythmLayerType layerType) {
        return findOrCreateLayer(layerType).getRhythmLayer();
    }

    public Set<RhythmLayerType> getRhythmLayerTypes() {
        return layerMap.keySet();
    }

    public Movement getLastMovement(RhythmLayerType rhythmLayerType) {


        return findOrCreateLayer(rhythmLayerType).getLastMovement();
    }



    public List<RhythmLayer> getRhythmLayers() {
        List<RhythmLayer> rhythmLayers = new ArrayList<>();

        for (RhythmLayerMapper layerMapper : layerMap.values()) {
            if (!layerMapper.isEmpty()) {
                rhythmLayers.add(layerMapper.getRhythmLayer());
            }
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
            subContext.saveFeature(key, movement);
        }

        Movement getLastMovement(RhythmLayerType layerType) {
            String key = layerType.name() + ".last.movement";
            return (Movement) subContext.getFeature(key);
        }

        public Set<RhythmLayerType> getLayerTypes() {
            Set<String> keys = subContext.featureKeySet();

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
            subContext.saveFeatureList(key, carryOverMovements);
        }

        public List<? extends Movement> getCarryOverMovements(RhythmLayerType layerType) {
            String key = layerType.name() + ".carry.over.movements";
            return (List<? extends Movement>) subContext.getFeatureList(key);
        }
    }


}
