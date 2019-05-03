package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface StoryGridMetricsRepository extends CrudRepository<StoryGridMetricsEntity, UUID> {

    List<StoryGridMetricsEntity> findByTorchieId(UUID torchieId);
}
