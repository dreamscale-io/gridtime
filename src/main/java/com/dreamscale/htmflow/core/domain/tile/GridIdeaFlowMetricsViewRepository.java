package com.dreamscale.htmflow.core.domain.tile;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface GridIdeaFlowMetricsViewRepository extends CrudRepository<GridIdeaFlowMetricsViewEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from grid_idea_flow_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and tile_seq >= (:start) and tile_seq <= (:end) "+
            "order by tile_seq")
    List<GridIdeaFlowMetricsViewEntity> findByTorchieZoomRange(@Param("torchieId") UUID torchieId,
                                                           @Param("zoom") String zoomLevel,
                                                           @Param("start") Long sequenceStart,
                                                           @Param("end") Long sequenceEnd);


    @Query(nativeQuery = true, value = "select ifm.* from grid_idea_flow_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and clock_time = (:clock) ")
    GridIdeaFlowMetricsViewEntity findByTorchieGridTime(@Param("torchieId") UUID torchieId,
                                                    @Param("zoom") String zoomLevel,
                                                    @Param("clock") Timestamp clockTime);
}
