package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridRowRepository extends CrudRepository<GridRowEntity, UUID> {

    List<GridRowEntity> findByTorchieIdAndZoomLevelAndRowNameOrderByTileSeq(UUID torchieId, ZoomLevel zoomLevel, String rowName);
}
