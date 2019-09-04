package com.dreamscale.gridtime.core.domain.tile.zoomable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ZoomableIdeaFlowMetricsRepository extends CrudRepository<ZoomableIdeaFlowMetricsEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from zoomable_idea_flow_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and tile_seq >= (:start) and tile_seq <= (:end) "+
            "order by tile_seq")
    List<ZoomableIdeaFlowMetricsEntity> findByTorchieZoomRange(@Param("torchieId") UUID torchieId,
                                                               @Param("zoom") String zoomLevel,
                                                               @Param("start") Long sequenceStart,
                                                               @Param("end") Long sequenceEnd);


    @Query(nativeQuery = true, value = "select ifm.* from zoomable_idea_flow_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and clock_time = (:clock) ")
    ZoomableIdeaFlowMetricsEntity findByTorchieGridTime(@Param("torchieId") UUID torchieId,
                                                        @Param("zoom") String zoomLevel,
                                                        @Param("clock") Timestamp clockTime);
}
