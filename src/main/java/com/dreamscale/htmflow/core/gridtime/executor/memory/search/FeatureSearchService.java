package com.dreamscale.htmflow.core.gridtime.executor.memory.search;

import com.dreamscale.htmflow.core.domain.tile.GridFeatureEntity;
import com.dreamscale.htmflow.core.domain.tile.GridFeatureRepository;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.JSONTransformer;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details.FeatureDetails;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.service.ComponentLookupService;
import com.dreamscale.htmflow.core.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class FeatureSearchService {

    @Autowired
    TeamService teamService;

    @Autowired
    ComponentLookupService componentLookupService;

    @Autowired
    GridFeatureRepository gridFeatureRepository;


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
                FeatureDetails feature = deserialize(gridFeatureEntity.getJson(), gridFeatureEntity.getFeatureType().getSerializationClass());

                resolvedReference = originalReference.resolve(gridFeatureEntity.getId(), feature);
            } else {
                String json = serialize(originalReference.getDetails());
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

    private String serialize(FeatureDetails feature) {
        return JSONTransformer.toJson(feature);
    }

    private FeatureDetails deserialize(String json, Class<? extends FeatureDetails> serializationClass) {
        return JSONTransformer.fromJson(json, serializationClass);
    }



}
