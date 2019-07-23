package com.dreamscale.htmflow.core.domain.time;

import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
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
}
