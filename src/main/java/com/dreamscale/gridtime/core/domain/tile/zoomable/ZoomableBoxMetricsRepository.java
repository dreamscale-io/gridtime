package com.dreamscale.gridtime.core.domain.tile.zoomable;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ZoomableBoxMetricsRepository extends CrudRepository<ZoomableBoxMetricsEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from zoomable_box_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and tile_seq >= (:start) and tile_seq <= (:end) "+
            "order by tile_seq, box_feature_id")
    List<ZoomableBoxMetricsEntity> findByTorchieZoomRange(@Param("torchieId") UUID torchieId,
                                                               @Param("zoom") String zoomLevel,
                                                               @Param("start") Long sequenceStart,
                                                               @Param("end") Long sequenceEnd);


    @Query(nativeQuery = true, value = "select * from zoomable_box_metrics_v " +
            "where torchie_id=(:torchieId) " +
            "and zoom_level=(:zoom) " +
            "and clock_time = (:clock) " +
            "order by box_feature_id")
    List<ZoomableBoxMetricsEntity> findByTorchieGridTime(@Param("torchieId") UUID torchieId,
                                                        @Param("zoom") String zoomLevel,
                                                        @Param("clock") Timestamp clockTime);
}
