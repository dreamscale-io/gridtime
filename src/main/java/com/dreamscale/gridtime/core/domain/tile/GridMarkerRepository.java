package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridMarkerRepository extends CrudRepository<GridMarkerEntity, UUID> {

    List<GridMarkerEntity> findByTorchieIdAndRowNameOrderByTileSeq(UUID torchieId, String rowName);

    List<GridMarkerEntity> findByTorchieIdOrderByTileSeq(UUID torchieId);
}
