package com.dreamscale.gridtime.core.domain.time;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GridTimeCalendarRepository extends CrudRepository<GridTimeCalendarEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_time_calendar " +
            "where zoom_level = (:zoom) order by tile_seq desc limit 1")
    GridTimeCalendarEntity getLast(@Param("zoom") String zoomLevel);

    GridTimeCalendarEntity findByZoomLevelAndClockTime(ZoomLevel zoomLevel, LocalDateTime clockTime);

    List<GridTimeCalendarEntity> findByZoomLevel(ZoomLevel zoomLevel);

    GridTimeCalendarEntity findByZoomLevelAndTileSeq(ZoomLevel zoomLevel, Long tileSeq);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_time_calendar")
    void truncate();
}
