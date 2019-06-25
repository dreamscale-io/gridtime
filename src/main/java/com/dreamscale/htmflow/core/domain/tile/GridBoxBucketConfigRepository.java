package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridBoxBucketConfigRepository extends CrudRepository<GridBoxBucketConfigEntity, UUID> {

    List<GridBoxBucketConfigEntity> findByTeamId(UUID teamId);
}
