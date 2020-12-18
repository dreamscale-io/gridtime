package com.dreamscale.gridtime.core.domain.time;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface GridCalendarRepository extends CrudRepository<GridCalendarEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = (:zoom) order by tile_seq desc limit 1")
    GridCalendarEntity getLast(@Param("zoom") String zoomLevel);

    GridCalendarEntity findByZoomLevelAndStartTime(ZoomLevel zoomLevel, LocalDateTime clockTime);

    List<GridCalendarEntity> findByZoomLevel(ZoomLevel zoomLevel);

    GridCalendarEntity findByZoomLevelAndTileSeq(ZoomLevel zoomLevel, Long tileSeq);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_calendar")
    void truncate();


    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = (:zoom) and start_time <= (:now) order by tile_seq desc limit 1")
    GridCalendarEntity findTileStartingBeforeTime(@Param("zoom") String zoomLevel, @Param("now") Timestamp now);


    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'YEAR' and year = (:year) limit 1 ")
    GridCalendarEntity findYearTileByCoords(@Param("year") Integer year);

    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'BLOCK' and year = (:year) and block = (:block) limit 1 ")
    GridCalendarEntity findBlockTileByCoords(@Param("year") Integer year, @Param("block") Integer block);

    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'WEEK' and year = (:year) and block = (:block) and block_week = (:week) limit 1 ")
    GridCalendarEntity findWeekTileByCoords(@Param("year") Integer year, @Param("block") Integer block, @Param("week") Integer week);

    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'DAY' and year = (:year) and block = (:block) " +
            "and block_week = (:week) and day = (:day) limit 1 ")
    GridCalendarEntity findDayTileByCoords(@Param("year") Integer year, @Param("block") Integer block,
                                           @Param("week") Integer week, @Param("day") Integer day);

    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'DAY_PART' and year = (:year) and block = (:block) " +
            "and block_week = (:week) and day = (:day) and day_part = (:daypart) limit 1 ")
    GridCalendarEntity findDayPartTileByCoords(@Param("year") Integer year, @Param("block") Integer block,
                                               @Param("week") Integer week, @Param("day") Integer day,
                                               @Param("daypart") Integer daypart);


    @Query(nativeQuery = true, value = "select * from grid_calendar " +
            "where zoom_level = 'TWENTY' and year = (:year) and block = (:block) and block_week = (:week) " +
            "and day = (:day) and day_part = (:daypart) and twenty_of_twelve = (:twenty) ")
    GridCalendarEntity findTwentyTileByCoords(@Param("year") Integer year, @Param("block") Integer block,
                                        @Param("week") Integer week, @Param("day") Integer day, @Param("daypart") Integer daypart,
                                        @Param("twenty") Integer twenty);

    @Query(nativeQuery = true, value = "select gc.* from grid_calendar gc " +
            "where gc.id = (select calendar_id from terminal_circuit_location_history lh " +
            "where lh.organization_id = (:organizationId) and lh.circuit_id = (:circuitId) " +
            "order by movement_date desc limit 1) ")
    GridCalendarEntity findTileByLastCircuitLocationHistory( @Param("organizationId") UUID organizationId, @Param("circuitId" ) UUID circuitId);
}
