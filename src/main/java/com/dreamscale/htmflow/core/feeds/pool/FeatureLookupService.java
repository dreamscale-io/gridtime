package com.dreamscale.htmflow.core.feeds.pool;

import com.dreamscale.htmflow.core.domain.tile.GridFeatureEntity;
import com.dreamscale.htmflow.core.domain.tile.GridFeatureRepository;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.JSONTransformer;
import com.dreamscale.htmflow.core.feeds.pool.feature.*;
import com.dreamscale.htmflow.core.feeds.story.feature.context.StructureLevel;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeatureLookupService {

    @Autowired
    ComponentLookupService componentLookupService;

    @Autowired
    GridFeatureRepository gridFeatureRepository;

    public FeatureReference createLocationReference(UUID projectId, String locationPath) {
        String boxName = componentLookupService.lookupComponent(projectId, locationPath);

        LocationInBox location = new LocationInBox(boxName, locationPath);

        return new FeatureReference(FeatureType.LOCATION_IN_BOX, location.toSearchKey(), location);
    }

    public FeatureReference createBoxReference(String boxName) {
        Box box = new Box(boxName);

        return new FeatureReference(FeatureType.BOX, box.toSearchKey(), box);
    }

    public FeatureReference createTraversalReference(UUID projectId, String fromLocation, String toLocation) {
        String fromBox = componentLookupService.lookupComponent(projectId, fromLocation);
        String toBox = componentLookupService.lookupComponent(projectId, toLocation);

        FeatureReference featureReference = null;
        if (isSameBox(fromBox, toBox)) {
            Traversal traversal = new Traversal(fromBox, fromLocation, toLocation);

            featureReference = new FeatureReference(FeatureType.TRAVERSAL_IN_BOX, traversal.toSearchKey(), traversal);
        } else {
            Bridge bridge = new Bridge(fromBox, fromLocation, toBox, toLocation);

            featureReference = new FeatureReference(FeatureType.BRIDGE_BETWEEN_BOXES, bridge.toSearchKey(), bridge);
        }

        return featureReference;
    }

    public FeatureReference createContextReference(StructureLevel structureLevel, UUID referenceId, String description) {
        Context context = new Context(structureLevel, referenceId, description);

        return new FeatureReference(FeatureType.getContextType(structureLevel), context.toSearchKey(), context);
    }

    private boolean isSameBox(String boxNameA, String boxNameB) {
        return boxNameA != null && boxNameA.equals(boxNameB);
    }


    public FeatureReference resolve(UUID teamId, FeatureReference originalReference) {

        FeatureReference resolvedReference;

        if (originalReference.isResolved()) {
            resolvedReference = originalReference;
        } else {
            GridFeatureEntity gridFeatureEntity = gridFeatureRepository.findByTeamIdAndAndSearchKey(teamId, originalReference.getSearchKey());

            if (gridFeatureEntity != null) {
                GridFeature feature = deserialize(gridFeatureEntity.getJson(), gridFeatureEntity.getFeatureType().getSerializationClass());

                resolvedReference = originalReference.resolve(gridFeatureEntity.getId(), feature);
            } else {
                String json = serialize(originalReference.getFeature());
                gridFeatureEntity = new GridFeatureEntity(
                        originalReference.getId(),
                        teamId,
                        originalReference.getFeatureType(),
                        originalReference.getSearchKey(),
                        json);
                gridFeatureRepository.save(gridFeatureEntity);

                resolvedReference = originalReference.resolve();
            }
        }

        return resolvedReference;
    }

    private String serialize(GridFeature feature) {
        return JSONTransformer.toJson(feature);
    }

    private GridFeature deserialize(String json, Class<? extends GridFeature> serializationClass) {
        return JSONTransformer.fromJson(json, serializationClass);
    }



}
