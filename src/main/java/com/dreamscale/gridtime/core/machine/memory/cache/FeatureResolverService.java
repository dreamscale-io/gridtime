package com.dreamscale.gridtime.core.machine.memory.cache;

import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.tile.GridFeatureEntity;
import com.dreamscale.gridtime.core.domain.tile.GridFeatureRepository;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.feature.details.Box;
import com.dreamscale.gridtime.core.machine.memory.feature.details.FeatureDetails;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.TypeRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.UUID;

@Slf4j
@Component
public class FeatureResolverService {

    @Autowired
    TeamCapability teamCapability;

    @Autowired
    GridFeatureRepository gridFeatureRepository;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    private final TypeRegistry typeRegistry;
    private final FeatureReferenceFactory featureFactory;

    private EntityManager entityManager;

    FeatureResolverService() {
        typeRegistry = new TypeRegistry();
        featureFactory = new FeatureReferenceFactory();
    }

    @PostConstruct
    void init() {
        entityManager = entityManagerFactory.createEntityManager();
    }

    private boolean isSameBox(String boxNameA, String boxNameB) {
        return boxNameA != null && boxNameA.equals(boxNameB);
    }

    public FeatureReference lookupById(UUID organizationId, UUID featureId) {

        GridFeatureEntity featureEntity = gridFeatureRepository.findByOrganizationIdAndId(organizationId, featureId);

        if (featureEntity != null) {
            FeatureType featureType = lookupFeatureType(featureEntity.getTypeUri());

            FeatureDetails details = deserialize(featureEntity.getJson(), featureType.getSerializationClass());

            return featureFactory.createResolvedFeatureReference(featureId, featureType, featureEntity.getSearchKey(), details);
        }

        log.warn("Feature not found with id :"+featureId);
        return null;
    }


    public void resolve(UUID organizationId, FeatureReference originalReference) {
        if (!originalReference.isResolved()) {

            synchronized (this) {
                if (!originalReference.isResolved()) {
                    tryToResolve(organizationId, originalReference);
                }
            }
        }
    }

    protected void tryToResolve(UUID organizationId, FeatureReference originalReference) {

        GridFeatureEntity gridFeatureEntity = gridFeatureRepository.findByOrganizationIdAndSearchKey(organizationId, originalReference.getSearchKey());

        if (gridFeatureEntity != null) {

            FeatureType featureType = lookupFeatureType(gridFeatureEntity.getTypeUri());

            FeatureDetails feature = deserialize(gridFeatureEntity.getJson(), featureType.getSerializationClass());

            originalReference.resolve(gridFeatureEntity.getId(), feature);
        } else {

            String json = serialize(originalReference.getDetails());
            GridFeatureEntity newFeature = new GridFeatureEntity(
                    originalReference.getFeatureId(),
                    organizationId,
                    originalReference.getFeatureType().getTypeUri(),
                    originalReference.getSearchKey(),
                    json);

            entityManager.getTransaction().begin();

            gridFeatureRepository.save(newFeature);

            entityManager.getTransaction().commit();

            log.debug("Created Feature Reference " + originalReference.getSearchKey());

            originalReference.resolve();
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
