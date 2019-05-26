package com.dreamscale.htmflow.core.feeds.pool;

import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import org.apache.commons.collections.map.LRUMap;

import java.util.UUID;

public class FeatureCache {

    private static final int BOX_CACHE_SIZE = 50;
    private static final int BRIDGE_CACHE_SIZE = 50;
    private static final int LOCATION_CACHE_SIZE = 100;
    private static final int TRAVERSAL_CACHE_SIZE = 200;
    private static final int CONTEXT_CACHE_SIZE = 50;

    private LRUMap boxCache = new LRUMap(BOX_CACHE_SIZE);
    private LRUMap bridgeCache = new LRUMap(BRIDGE_CACHE_SIZE);
    private LRUMap locationCache = new LRUMap(LOCATION_CACHE_SIZE);
    private LRUMap traversalCache = new LRUMap(TRAVERSAL_CACHE_SIZE);
    private LRUMap contextCache = new LRUMap(CONTEXT_CACHE_SIZE);

    private final UUID teamId;
    private final FeatureLookupService featureLookupService;


    public FeatureCache(UUID teamId, FeatureLookupService featureLookupService) {
        this.teamId = teamId;
        this.featureLookupService = featureLookupService;
    }

    public FeatureReference lookupContextReference(StructureLevel structureLevel, UUID referenceId, String description) {
        FeatureReference contextReference = featureLookupService.createContextReference(structureLevel, referenceId, description);

        return cacheLookup(contextReference);
    }

    public FeatureReference lookupLocationReference(UUID projectId, String locationPath) {
        FeatureReference locationReference = featureLookupService.createLocationReference(projectId, locationPath);

        return cacheLookup(locationReference);
    }

    public FeatureReference lookupBoxReference(String boxName) {
        FeatureReference boxReference = featureLookupService.createBoxReference(boxName);

        return cacheLookup(boxReference);
    }

    public FeatureReference lookupTraversalReference(UUID projectId, String fromLocation, String toLocation) {
        FeatureReference traversalReference = featureLookupService.createTraversalReference(projectId, fromLocation, toLocation);

        return cacheLookup(traversalReference);
    }


    private FeatureReference cacheLookup(FeatureReference fallbackReference) {
        LRUMap cache = getCache(fallbackReference.getFeatureType());

        FeatureReference cachedReference = (FeatureReference) cache.get(fallbackReference.getSearchKey());
        if (cachedReference == null) {
            cachedReference = fallbackReference;
            cache.put(fallbackReference.getSearchKey(), fallbackReference);
        }

        return cachedReference;
    }

    public FeatureReference resolve(FeatureReference reference) {
        FeatureReference resolvedReference = null;
        if (reference.isResolved()) {
            resolvedReference = reference;
        }
        else {
            resolvedReference = featureLookupService.resolve(teamId, reference);
        }
        return resolvedReference;
    }

    private LRUMap getCache(FeatureType featureType) {
        switch (featureType) {
            case BOX:
                return boxCache;

            case BRIDGE_BETWEEN_BOXES:
                return bridgeCache;

            case LOCATION_IN_BOX:
                return locationCache;

            case TRAVERSAL_IN_BOX:
                return traversalCache;

            case PROJECT_CONTEXT:
            case TASK_CONTEXT:
            case INTENTION_CONTEXT:
                return contextCache;
        }

        return null;
    }


}
