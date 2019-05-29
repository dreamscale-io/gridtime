package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridFeatureRepository extends CrudRepository<GridFeatureEntity, UUID> {

    GridFeatureEntity findByTeamIdAndAndSearchKey(UUID teamId, String searchKey);
}
