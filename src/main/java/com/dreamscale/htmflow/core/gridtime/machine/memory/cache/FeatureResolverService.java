package com.dreamscale.htmflow.core.gridtime.machine.memory.cache;

import com.dreamscale.htmflow.core.domain.tile.GridFeatureEntity;
import com.dreamscale.htmflow.core.domain.tile.GridFeatureRepository;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.FeatureType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.type.TypeRegistry;
import com.dreamscale.htmflow.core.gridtime.machine.commons.JSONTransformer;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeatureResolverService {

    @Autowired
    TeamService teamService;

    @Autowired
    GridFeatureRepository gridFeatureRepository;

    private TypeRegistry typeRegistry;

    FeatureResolverService() {
        typeRegistry = new TypeRegistry();
    }

    private boolean isSameBox(String boxNameA, String boxNameB) {
        return boxNameA != null && boxNameA.equals(boxNameB);
    }


    public void resolve(UUID teamId, FeatureReference originalReference) {

        if (!originalReference.isResolved()) {
            GridFeatureEntity gridFeatureEntity = gridFeatureRepository.findByTeamIdAndAndSearchKey(teamId, originalReference.getSearchKey());

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
