package com.dreamscale.gridtime.core.domain.tile.metrics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface GridBoxMetricsRepository extends CrudRepository<GridBoxMetricsEntity, UUID> {

    @Query(nativeQuery = true, value = "select bm.* from grid_box_metrics bm, grid_calendar gtc " +
            "where bm.torchie_id=(:torchieId) " +
            "and bm.calendar_id = gtc.id " +
            "and gtc.zoom_level = (:zoom) " +
            "and gtc.start_time = (:clock) ")
    List<GridBoxMetricsEntity> findByTorchieGridTime(@Param("torchieId") UUID torchieId,
                                                     @Param("zoom") String zoomLevel,
                                                     @Param("clock") Timestamp clockTime);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_box_metrics")
    void truncate();
}
