package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface StoryTileRepository extends CrudRepository<StoryTileEntity, UUID> {

    List<StoryTileEntity> findByTorchieIdOrderByClockPosition(UUID torchieId);

    StoryTileEntity findByTorchieIdAndDreamTime(UUID torchieId, String dreamTime);
}
