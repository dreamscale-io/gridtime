package com.dreamscale.gridtime.core.domain.tile.zoomable;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface ZoomableTeamBoxMetricsRepository extends CrudRepository<ZoomableTeamBoxMetricsEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from zoomable_team_box_metrics_v " +
            "where team_id=(:teamId) " +
            "and zoom_level=(:zoom) " +
            "and tile_seq >= (:start) and tile_seq <= (:end) "+
            "order by tile_seq")
    List<ZoomableTeamBoxMetricsEntity> findByTeamZoomRange(@Param("teamId") UUID torchieId,
                                                                @Param("zoom") String zoomLevel,
                                                                @Param("start") Long sequenceStart,
                                                                @Param("end") Long sequenceEnd);


    @Query(nativeQuery = true, value = "select * from zoomable_team_box_metrics_v " +
            "where team_id=(:teamId) " +
            "and zoom_level=(:zoom) " +
            "and clock_time = (:clock) ")
    List<ZoomableTeamBoxMetricsEntity> findByTeamGridTime(@Param("teamId") UUID teamId,
                                                         @Param("zoom") String zoomLevel,
                                                         @Param("clock") Timestamp clockTime);


    List<ZoomableTeamBoxMetricsEntity> findByTeamIdAndZoomLevelAndTileSeq(UUID teamId, ZoomLevel zoomLevel, Long tileSeq);
}
