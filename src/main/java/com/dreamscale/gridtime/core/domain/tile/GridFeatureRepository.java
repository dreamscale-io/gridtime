package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridFeatureRepository extends CrudRepository<GridFeatureEntity, UUID> {

    GridFeatureEntity findByOrganizationIdAndSearchKey(UUID organizationId, String searchKey);

    GridFeatureEntity findByOrganizationIdAndId(UUID organizationId, UUID featureId);
}
