package com.dreamscale.gridtime.core.machine.memory.grid.space;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.ExecutionReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.glyph.GlyphReferences;
import com.dreamscale.gridtime.core.machine.memory.grid.space.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.space.metrics.BridgeMetrics;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

public class GravityModel {

    //model the focal points in here,

    private final Map<PlaceReference, BoxMetrics> boxMetricsMap = DefaultCollections.map();
    private final Map<PlaceReference, BridgeMetrics> bridgeMetricsMap = DefaultCollections.map();
    private final FeatureCache featureCache;

    private PlaceReference lastLocation;
    private PlaceReference lastBox;

    public GravityModel(FeatureCache featureCache, GlyphReferences glyphReferences) {
        this.featureCache = featureCache;
    }


    public void gotoLocation(LocalDateTime moment, PlaceReference locationReference, Duration timeInLocation) {

        PlaceReference boxReference = featureCache.lookupBoxReference(locationReference);
        BoxMetrics boxMetrics = findOrCreateBoxMetrics(boxReference);
        boxMetrics.visitLocation(locationReference, timeInLocation);

        if (lastLocation != null) {
            PlaceReference traversalReference = featureCache.lookupTraversalReference(lastLocation, locationReference);
            if (traversalReference.getPlaceType() == PlaceType.BRIDGE_BETWEEN_BOXES) {
                visitBridge(traversalReference);
            } else {
                boxMetrics.visitTraversal(traversalReference);
            }
        }

        lastBox = boxReference;

    }

    private void visitBridge(PlaceReference bridgeReference) {
        BridgeMetrics bridgeMetrics = findOrCreateBridgeMetrics(bridgeReference);
        bridgeMetrics.visit();

    }

    private BoxMetrics findOrCreateBoxMetrics(PlaceReference boxReference ) {
        BoxMetrics boxMetrics = boxMetricsMap.get(boxReference);
        if (boxMetrics == null) {
            boxMetrics = new BoxMetrics(boxReference);
            boxMetricsMap.put(boxReference, boxMetrics);
        }
        return boxMetrics;
    }

    private BridgeMetrics findOrCreateBridgeMetrics(PlaceReference bridgeReference ) {
        BridgeMetrics bridgeMetrics = bridgeMetricsMap.get(bridgeReference);
        if (bridgeMetrics == null) {
            bridgeMetrics = new BridgeMetrics(bridgeReference);
            bridgeMetricsMap.put(bridgeReference, bridgeMetrics);
        }
        return bridgeMetrics;
    }

    public void modifyInPlace(PlaceReference location, int modificationCount) {
        PlaceReference boxReference = featureCache.lookupBoxReference(location);

        BoxMetrics boxMetrics = findOrCreateBoxMetrics(boxReference);
        boxMetrics.modify(modificationCount);

    }

    public void executeInPlace(PlaceReference location, ExecutionReference executionReference) {
        PlaceReference boxReference = featureCache.lookupBoxReference(location);

        BoxMetrics boxMetrics = findOrCreateBoxMetrics(boxReference);
        boxMetrics.execute(executionReference);
    }
}
