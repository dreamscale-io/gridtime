package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TorchieBookmarkRepository extends CrudRepository<TorchieBookmarkEntity, UUID> {

    public TorchieBookmarkEntity findByTorchieId(UUID torchieId);
}
