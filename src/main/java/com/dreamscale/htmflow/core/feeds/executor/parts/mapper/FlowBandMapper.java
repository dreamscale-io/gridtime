package com.dreamscale.htmflow.core.feeds.executor.parts.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.music.BeatsPerBucket;
import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;
import com.dreamscale.htmflow.core.feeds.story.feature.timeband.*;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.layer.BandLayerMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;

import java.time.LocalDateTime;
import java.util.*;

public class FlowBandMapper {

    private final LocalDateTime from;
    private final LocalDateTime to;
    private final MusicGeometryClock internalClock;
    private final FeatureFactory featureFactory;

    private Map<BandLayerType, BandLayerMapper> layerMap = new HashMap<>();

    public FlowBandMapper(FeatureFactory featureFactory, LocalDateTime from, LocalDateTime to) {
        this.featureFactory = featureFactory;
        this.internalClock = new MusicGeometryClock(from, to);
        this.from = from;
        this.to = to;

    }

    private BandLayerMapper findOrCreateLayer(BandLayerType layerType) {
        BandLayerMapper layer = this.layerMap.get(layerType);
        if (layer == null) {
            layer = new BandLayerMapper(featureFactory, internalClock, layerType);
            this.layerMap.put(layerType, layer);
        }
        return layer;
    }


    public CarryOverContext getCarryOverContext() {
        CarryOverSubContext subContext = new CarryOverSubContext();

        for (BandLayerMapper layer : this.layerMap.values()) {
            subContext.setLastBand(layer.getLayerType(), layer.getLastBand());
            subContext.setActiveBandContext(layer.getLayerType(), layer.getActiveDetails());
        }

        return subContext.toCarryOverContext();
    }

    public void initFromCarryOverContext(CarryOverContext carryOverContext) {

        CarryOverSubContext subContext = new CarryOverSubContext(carryOverContext);

        Set<BandLayerType> layerTypes = subContext.getLayerTypes();

        for (BandLayerType layerType : layerTypes) {
            BandLayerMapper layer = findOrCreateLayer(layerType);

            Timeband lastBand = subContext.getLastBand(layerType);
            Details activeContext = subContext.getActiveBandContext(layerType);
            layer.initContext(lastBand, activeContext);
        }

    }

    public void startBand(BandLayerType bandLayerType, LocalDateTime startBandPosition, Details details) {
        BandLayerMapper layer = findOrCreateLayer(bandLayerType);

        layer.startBand(startBandPosition, details);
    }

    public void clearBand(BandLayerType bandLayerType, LocalDateTime endBandPosition) {
        BandLayerMapper layer = findOrCreateLayer(bandLayerType);

        layer.clearBand(endBandPosition);
    }

    public void finish() {
        for (BandLayerMapper layer: layerMap.values()) {
            layer.finish();
        }
    }

    public TimebandLayer getBandLayer(BandLayerType layerType) {
        return findOrCreateLayer(layerType).getLayer();
    }

    public List<TimebandLayer> getBandLayers() {
        List<TimebandLayer> layers = new ArrayList<>();
        for (BandLayerType layerType : layerMap.keySet()) {
            layers.add(getBandLayer(layerType));
        }

        return layers;
    }


    public Set<BandLayerType> getBandLayerTypes() {
        return layerMap.keySet();
    }

    public Timeband getLastBand(BandLayerType bandLayerType) {
        return this.layerMap.get(bandLayerType).getLastBand();
    }

    public void configureRollingBands(BandLayerType bandLayerType, BeatsPerBucket beatsPerBucket) {
        BandLayerMapper layer = findOrCreateLayer(bandLayerType);
        layer.fillWithRollingAggregateBands(beatsPerBucket);
    }

    public void addRollingBandSample(BandLayerType bandLayerType, LocalDateTime moment, double sample) {
        BandLayerMapper layer = findOrCreateLayer(bandLayerType);

        if (!layer.isRollingBandLayerConfigured()) {
            configureRollingBands(bandLayerType, BeatsPerBucket.QUARTER);
        }

        layer.addRollingBandSample(moment, sample);
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

        void setActiveBandContext(BandLayerType layerType, Details details) {
            String key = layerType.name() + ".active.context";
            subContext.saveDetails(key, details);
        }

        Details getActiveBandContext(BandLayerType layerType) {
            String key = layerType.name() + ".active.context";
            return subContext.getDetails(key);
        }

        void setLastBand(BandLayerType layerType, Timeband timeBand) {
            String key = layerType.name() + ".last.band";
            subContext.saveFeature(key, timeBand);
        }

        Timeband getLastBand(BandLayerType layerType) {
            String key = layerType.name() + ".last.band";
            return (Timeband) subContext.getFeature(key);
        }

        public Set<BandLayerType> getLayerTypes() {
            Set<String> keys = subContext.featureKeySet();

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
