package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.band.*;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer.BandLayerMapper;

import java.time.LocalDateTime;
import java.util.*;

public class FlowBandMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final InnerGeometryClock internalClock;

    private Map<BandLayerType, BandLayerMapper> layerMap = new HashMap<>();

    public FlowBandMapper(LocalDateTime from, LocalDateTime to) {
        this.from = from;
        this.to = to;

        this.internalClock = new InnerGeometryClock(from, to);
    }


    private BandLayerMapper findOrCreateLayer(BandLayerType layerType) {
        BandLayerMapper layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new BandLayerMapper(internalClock, layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }


    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();

        for (BandLayerMapper layer : this.layerMap.values()) {
            subContext.setLastBand(layer.getLayerType(), layer.getLastBand());
            subContext.setActiveBandContext(layer.getLayerType(), layer.getActiveBandContext());
        }

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Set<BandLayerType> layerTypes = subContext.getLayerTypes();

        for (BandLayerType layerType : layerTypes) {
            BandLayerMapper layer = findOrCreateLayer(layerType);

            TimeBand lastBand = subContext.getLastBand(layerType);
            BandContext activeContext = subContext.getActiveBandContext(layerType);
            layer.initContext(lastBand, activeContext);
        }

    }

    public void startBand(BandLayerType bandLayerType, LocalDateTime startBandPosition, BandContext bandContext) {
        BandLayerMapper layer = layerMap.get(bandLayerType);

        layer.startBand(startBandPosition, bandContext);
    }

    public void clearBand(BandLayerType bandLayerType, LocalDateTime endBandPosition) {
        BandLayerMapper layer = layerMap.get(bandLayerType);

        layer.clearBand(endBandPosition);
    }

    public void finish() {
        for (BandLayerMapper layer: layerMap.values()) {
            layer.finish();
        }
    }

    public TimeBandLayer getBandLayer(BandLayerType layerType) {
        return new TimeBandLayer(from, to, layerMap.get(layerType).getTimeBands());
    }

    public Set<BandLayerType> getBandLayerTypes() {
        return layerMap.keySet();
    }

    public TimeBand getLastBand(BandLayerType bandLayerType) {
        return this.layerMap.get(bandLayerType).getLastBand();
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

        void setActiveBandContext(BandLayerType layerType, BandContext bandContext) {
            String key = layerType.name() + ".active.context";
            subContext.addKeyValue(key, bandContext);
        }

        BandContext getActiveBandContext(BandLayerType layerType) {
            String key = layerType.name() + ".active.context";
            return (BandContext) subContext.getValue(key);
        }

        void setLastBand(BandLayerType layerType, TimeBand timeBand) {
            String key = layerType.name() + ".last.band";
            subContext.addKeyValue(key, timeBand);
        }

        TimeBand getLastBand(BandLayerType layerType) {
            String key = layerType.name() + ".last.band";
            return (TimeBand) subContext.getValue(key);
        }

        public Set<BandLayerType> getLayerTypes() {
            Set<String> keys = subContext.keySet();

            Set<BandLayerType> layerTypes = new LinkedHashSet<>();
            for (String key : keys) {

                String layerTypeName = parseLayerTypeFromKey(key);
                layerTypes.add(BandLayerType.valueOf(layerTypeName));
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
