package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridBoxBucketConfigRepository extends CrudRepository<GridBoxBucketConfigEntity, UUID> {

    List<GridBoxBucketConfigEntity> findByProjectId(UUID projectId);
}
