package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.FeelsContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBandLayer;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.TimeBand;
import com.dreamscale.htmflow.core.feeds.story.mapper.layer.TimeBandLayerMapper;

import java.time.LocalDateTime;
import java.util.*;

public class FlowBandMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final InnerGeometryClock internalClock;

    private InnerGeometryClock.Coords currentMoment;

    private Map<TimeBandLayerType, TimeBandLayerMapper> layerMap = new HashMap<>();

    public FlowBandMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;

        this.internalClock = new InnerGeometryClock(from, to);
    }


    private TimeBandLayerMapper findOrCreateLayer(TimeBandLayerType layerType) {
        TimeBandLayerMapper layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new TimeBandLayerMapper(layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }

    public void addBand(TimeBandLayerType layerType, TimeBand band) {
        TimeBandLayerMapper layer = findOrCreateLayer(layerType);

        if (band != null) {
            currentMoment = layer.addTimeBand(internalClock, band);
        }
    }

    public InnerGeometryClock.Coords getCurrentMoment() {
        return currentMoment;
    }


    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();

        for (TimeBandLayerMapper layer : this.layerMap.values()) {
            TimeBand lastBand = layer.getLastBand();
            subContext.addLastBand(layer.getLayerType(), lastBand);
        }

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Set<TimeBandLayerType> layerTypes = subContext.getLayerTypes();

        for (TimeBandLayerType layerType : layerTypes) {
            TimeBandLayerMapper layer = findOrCreateLayer(layerType);

            layer.initContext(subContext.getLastBand(layerType));

        }

    }


    public void finish() {
        for (TimeBandLayerMapper layer: layerMap.values()) {
            layer.repairSortingAndSequenceNumbers();
        }
    }

    public TimeBandLayer getTimeBandLayer(TimeBandLayerType layerType) {
        return new TimeBandLayer(from, to, layerMap.get(layerType).getTimeBands());
    }

    public Set<TimeBandLayerType> getTimeBandLayerTypes() {
        return layerMap.keySet();
    }

    public TimeBand getLastBand(TimeBandLayerType bandLayerType) {
        return this.layerMap.get(bandLayerType).getLastBand();
    }

    public void feel(LocalDateTime moment, FeelsContext feelsContext) {

    }


    public static class CarryOverSubContext {

        private static final String SUBCONTEXT_NAME = "[FlowBandMapper]";

        private final CarryOverContext subContext;

        public CarryOverSubContext() {
             subContext = new CarryOverContext(SUBCONTEXT_NAME);
        }

        public CarryOverSubContext(CarryOverContext mainContext) {
            subContext = mainContext.getSubContext(SUBCONTEXT_NAME);
        }

        void addLastBand(TimeBandLayerType layerType, TimeBand timeBand) {
            String key = layerType.name() + ".last.band";
            subContext.addKeyValue(key, timeBand);
        }

        TimeBand getLastBand(TimeBandLayerType layerType) {
            String key = layerType.name() + ".last.band";
            return (TimeBand) subContext.getValue(key);
        }

        public Set<TimeBandLayerType> getLayerTypes() {
            Set<String> keys = subContext.keySet();

            Set<TimeBandLayerType> layerTypes = new LinkedHashSet<>();
            for (String key : keys) {

                String layerTypeName = parseLayerTypeFromKey(key);
                layerTypes.add(TimeBandLayerType.valueOf(layerTypeName));
            }

            return layerTypes;
        }

        private String parseLayerTypeFromKey(String key) {
            return key.substring(0, key.indexOf("."));
        }

        public CarryOverContext toCarryOverContext() {
            return subContext;
        }

    }


}
