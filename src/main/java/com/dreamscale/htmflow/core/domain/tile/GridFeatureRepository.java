package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.feeds.pool.FeatureType;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridFeatureRepository extends CrudRepository<GridFeatureEntity, UUID> {

    GridFeatureEntity findByTeamIdAndAndSearchKey(UUID teamId, String searchKey);
}
