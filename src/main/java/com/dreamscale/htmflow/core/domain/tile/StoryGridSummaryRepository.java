package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface StoryGridSummaryRepository extends CrudRepository<StoryGridSummaryEntity, UUID> {

    List<StoryGridSummaryEntity> findByTorchieId(UUID torchieId);
}
