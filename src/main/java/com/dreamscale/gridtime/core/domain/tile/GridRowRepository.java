package com.dreamscale.gridtime.core.domain.tile;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridRowRepository extends CrudRepository<GridRowEntity, UUID> {

    List<GridRowEntity> findByTorchieIdAndZoomLevelAndRowNameOrderByTileSeq(UUID torchieId, ZoomLevel zoomLevel, String rowName);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_row")
    void truncate();

    List<GridRowEntity> findByTorchieIdAndZoomLevelAndTileSeq(UUID torchieId, ZoomLevel zoomLevel, Long tileSeq);
}
