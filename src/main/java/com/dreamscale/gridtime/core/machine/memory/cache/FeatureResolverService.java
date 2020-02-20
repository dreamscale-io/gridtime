package com.dreamscale.gridtime.core.machine.memory.cache;

import com.dreamscale.gridtime.core.domain.tile.GridFeatureEntity;
import com.dreamscale.gridtime.core.domain.tile.GridFeatureRepository;
import com.dreamscale.gridtime.core.machine.memory.feature.details.Box;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.TypeRegistry;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeatureResolverService {


    @Autowired
    TeamMembershipCapability teamMembership;

    @Autowired
    GridFeatureRepository gridFeatureRepository;

    private final TypeRegistry typeRegistry;
    private final FeatureReferenceFactory featureFactory;

    FeatureResolverService() {
        typeRegistry = new TypeRegistry();
        featureFactory = new FeatureReferenceFactory();
    }

    private boolean isSameBox(String boxNameA, String boxNameB) {
        return boxNameA != null && boxNameA.equals(boxNameB);
    }

    public PlaceReference lookupBox(UUID teamId, UUID boxFeatureId) {

        GridFeatureEntity boxFeatureEntity = gridFeatureRepository.findByTeamIdAndId(teamId, boxFeatureId);

        if (boxFeatureEntity != null) {
            FeatureType featureType = lookupFeatureType(boxFeatureEntity.getTypeUri());

            Box box = (Box) deserialize(boxFeatureEntity.getJson(), featureType.getSerializationClass());

            return featureFactory.createResolvedBoxReference(boxFeatureId, box);
        }

        return null;
    }

    public void resolve(UUID teamId, FeatureReference originalReference) {

        if (!originalReference.isResolved()) {
            GridFeatureEntity gridFeatureEntity = gridFeatureRepository.findByTeamIdAndSearchKey(teamId, originalReference.getSearchKey());

            if (gridFeatureEntity != null) {
                FeatureType featureType = lookupFeatureType(gridFeatureEntity.getTypeUri());

                FeatureDetails feature = deserialize(gridFeatureEntity.getJson(), featureType.getSerializationClass());

                originalReference.resolve(gridFeatureEntity.getId(), feature);
            } else {
                String json = serialize(originalReference.getDetails());
                gridFeatureEntity = new GridFeatureEntity(
                        originalReference.getFeatureId(),
                        teamId,
                        originalReference.getFeatureType().getTypeUri(),
                        originalReference.getSearchKey(),
                        json);
                gridFeatureRepository.save(gridFeatureEntity);

                originalReference.resolve();
            }
        }
    }

    private FeatureType lookupFeatureType(String typeUri) {
        return typeRegistry.resolveFeatureType(typeUri);
    }

    private String serialize(FeatureDetails feature) {
        if (feature != null) {
            return JSONTransformer.toJson(feature);
        } else {
            return null;
        }
    }

    private FeatureDetails deserialize(String json, Class<? extends FeatureDetails> serializationClass) {
        if (serializationClass != null) {
            return JSONTransformer.fromJson(json, serializationClass);
        } else {
            return null;
        }
    }



}
