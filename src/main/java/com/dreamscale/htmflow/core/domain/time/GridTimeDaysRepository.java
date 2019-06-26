package com.dreamscale.htmflow.core.domain.time;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GridTimeDaysRepository extends CrudRepository<GridTimeDaysEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_time_days " +
            "order by tile_seq desc limit 1")
    GridTimeDaysEntity getLast();

    GridTimeDaysEntity findByClockTime(LocalDateTime clockTime);
}
