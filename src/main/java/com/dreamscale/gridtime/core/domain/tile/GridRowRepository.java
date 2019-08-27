package com.dreamscale.gridtime.core.domain.tile;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridRowRepository extends CrudRepository<GridRowEntity, UUID> {

    List<GridRowEntity> findByTorchieIdAndZoomLevelAndRowNameOrderByTileSeq(UUID torchieId, ZoomLevel zoomLevel, String rowName);
}
