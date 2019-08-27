package com.dreamscale.gridtime.core.machine.memory.cache;

import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.feature.details.*;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.*;
import com.dreamscale.gridtime.core.machine.memory.type.*;
import org.apache.commons.collections.map.LRUMap;

import java.util.UUID;

public class FeatureCache {

    private static final int BOX_CACHE_SIZE = 50;
    private static final int BRIDGE_CACHE_SIZE = 50;
    private static final int LOCATION_CACHE_SIZE = 100;
    private static final int TRAVERSAL_CACHE_SIZE = 200;
    private static final int CONTEXT_CACHE_SIZE = 50;

    private static final int MISC_CACHE_SIZE = 100;

    private LRUMap boxCache = new LRUMap(BOX_CACHE_SIZE);
    private LRUMap bridgeCache = new LRUMap(BRIDGE_CACHE_SIZE);
    private LRUMap locationCache = new LRUMap(LOCATION_CACHE_SIZE);
    private LRUMap traversalCache = new LRUMap(TRAVERSAL_CACHE_SIZE);
    private LRUMap contextCache = new LRUMap(CONTEXT_CACHE_SIZE);

    private LRUMap miscCache = new LRUMap(MISC_CACHE_SIZE);

    private final TeamBoxConfiguration teamBoxConfiguration;
    private final FeatureReferenceFactory featureReferenceFactory;

    private static final UUID DEFAULT_PROJECT_ID = UUID.fromString("9ad99705-3ec3-4979-ab14-104fba3fd38f");

    public FeatureCache(TeamBoxConfiguration boxConfiguration) {
        this.teamBoxConfiguration = boxConfiguration;
        this.featureReferenceFactory = new FeatureReferenceFactory();
    }

    public FeatureCache() {
        this.teamBoxConfiguration = new TeamBoxConfiguration();
        this.featureReferenceFactory = new FeatureReferenceFactory();
    }

    public IdeaFlowStateReference lookupWTFReference(CircleDetails circleDetails) {
        IdeaFlowStateReference stateReference = featureReferenceFactory.createWTFReference(circleDetails);

        return cacheLookup(stateReference);
    }

    public IdeaFlowStateReference lookupIdeaFlowStateReference(IdeaFlowStateType stateType) {
        IdeaFlowStateReference stateReference = featureReferenceFactory.createIdeaFlowReference(stateType);

        return cacheLookup(stateReference);
    }

    public FeelsReference lookupFeelsStateReference(Integer feelsRating) {
        FeelsReference feelsState = featureReferenceFactory.createFeelsStateReference(feelsRating);

        return cacheLookup(feelsState);
    }

    public ExecutionReference lookupExecutionReference(ExecutionEvent executionEvent) {
        ExecutionReference executionReference = featureReferenceFactory.createExecutionReference(executionEvent);

        return cacheLookup(executionReference);
    }

    public WorkContextReference lookupDefaultProject() {
        return featureReferenceFactory.createWorkContextReference(StructureLevel.PROJECT, DEFAULT_PROJECT_ID, "Default");
    }

    public WorkContextReference lookupContextReference(StructureLevel structureLevel, UUID referenceId, String description) {
        WorkContextReference contextReference = featureReferenceFactory.createWorkContextReference(structureLevel, referenceId, description);

        return cacheLookup(contextReference);
    }

    public PlaceReference lookupLocationReference(UUID projectId, String locationPath) {
        String boxName = teamBoxConfiguration.identifyBox(projectId, locationPath);
        PlaceReference locationReference = featureReferenceFactory.createLocationReference(projectId, boxName, locationPath);

        return cacheLookup(locationReference);
    }

    public PlaceReference lookupBoxReference(PlaceReference locationReference) {
        LocationInBox locationInBox = locationReference.getPlaceDetails();

        PlaceReference boxReference = featureReferenceFactory.createBoxReference(locationInBox.getProjectId(), locationInBox.getBoxName());

        return cacheLookup(boxReference);
    }

    public PlaceReference lookupBoxReference(UUID projectId, String boxName) {
        PlaceReference boxReference = featureReferenceFactory.createBoxReference(projectId, boxName);

        return cacheLookup(boxReference);
    }

    public AuthorsReference lookupAuthorsReference(AuthorsDetails authorsDetails) {
        AuthorsReference authorsReference = featureReferenceFactory.createAuthorsReference(authorsDetails);

        return cacheLookup(authorsReference);
    }

    public PlaceReference lookupTraversalReference(PlaceReference fromLocation, PlaceReference toLocation) {
        LocationInBox fromLocationDetails = fromLocation.getPlaceDetails();
        LocationInBox toLocationDetails = toLocation.getPlaceDetails();

        PlaceReference traversalReference = featureReferenceFactory.createTraversalReference(fromLocationDetails, toLocationDetails);

        return cacheLookup(traversalReference);
    }

    private <T extends FeatureReference> T cacheLookup(T fallbackReference) {
        LRUMap cache = getCache(fallbackReference.getFeatureType());

        T cachedReference = (T) cache.get(fallbackReference.getSearchKey());
        if (cachedReference == null) {
            cachedReference = fallbackReference;
            cache.put(fallbackReference.getSearchKey(), fallbackReference);
        }

        return cachedReference;
    }

    private LRUMap getCache(FeatureType featureType) {
        if (featureType.equals(PlaceType.BOX)) {
            return boxCache;
        } else if (featureType.equals(PlaceType.BRIDGE_BETWEEN_BOXES)) {
            return bridgeCache;
        } else if (featureType.equals(PlaceType.LOCATION)) {
            return locationCache;
        } else if (featureType.equals(PlaceType.TRAVERSAL_IN_BOX)) {
            return traversalCache;
        } else if (featureType instanceof WorkContextType) {
               return contextCache;
        }
        return miscCache;
    }



}
