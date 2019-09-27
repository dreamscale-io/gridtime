package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridFeatureRepository extends CrudRepository<GridFeatureEntity, UUID> {

    GridFeatureEntity findByTeamIdAndSearchKey(UUID teamId, String searchKey);

    GridFeatureEntity findByTeamIdAndId(UUID teamId, UUID featureId);
}
