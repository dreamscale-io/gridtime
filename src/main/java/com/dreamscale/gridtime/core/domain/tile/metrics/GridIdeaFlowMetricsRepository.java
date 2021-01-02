package com.dreamscale.gridtime.core.domain.tile.metrics;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public interface GridIdeaFlowMetricsRepository extends CrudRepository<GridIdeaFlowMetricsEntity, UUID> {


    @Query(nativeQuery = true, value = "select ifm.* from grid_idea_flow_metrics ifm, grid_calendar gtc " +
            "where ifm.torchie_id=(:torchieId) " +
            "and ifm.calendar_id = gtc.id " +
            "and gtc.zoom_level=(:zoom) " +
            "and gtc.tile_seq >= (:start) and gtc.tile_seq <= (:end) "+
            "order by gtc.tile_seq")
    List<GridIdeaFlowMetricsEntity> findByTorchieZoomRange(@Param("torchieId") UUID torchieId,
                                                           @Param("zoom") String zoomLevel,
                                                           @Param("start") Long sequenceStart,
                                                           @Param("end") Long sequenceEnd);


    @Query(nativeQuery = true, value = "select ifm.* from grid_idea_flow_metrics ifm, grid_calendar gtc " +
            "where ifm.torchie_id=(:torchieId) " +
            "and ifm.calendar_id=gtc.id " +
            "and gtc.zoom_level = (:zoom) " +
            "and gtc.start_time = (:clock) ")
    GridIdeaFlowMetricsEntity findByTorchieGridTime(@Param("torchieId") UUID torchieId,
                                                    @Param("zoom") String zoomLevel,
                                                    @Param("clock") Timestamp clockTime);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_idea_flow_metrics")
    void truncate();

    @Modifying
    @Query(nativeQuery = true, value = "delete from grid_idea_flow_metrics where torchie_id=(:torchieId) and calendar_id=(:calendarId)")
    void deleteByTorchieIdAndCalendarId(@Param("torchieId") UUID torchieId, @Param("calendarId") UUID calendarId);

}
