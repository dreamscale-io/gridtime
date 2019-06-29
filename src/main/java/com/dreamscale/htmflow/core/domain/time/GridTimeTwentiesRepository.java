package com.dreamscale.htmflow.core.domain.time;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GridTimeTwentiesRepository extends CrudRepository<GridTimeTwentiesEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_time_twenties " +
            "order by tile_seq desc limit 1")
    GridTimeTwentiesEntity getLast();

    GridTimeTwentiesEntity findByClockTime(LocalDateTime clockTime);
}
