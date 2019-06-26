package com.dreamscale.htmflow.core.domain.time;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.UUID;

public interface GridTimeWeeksRepository extends CrudRepository<GridTimeWeeksEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_time_weeks " +
            "order by tile_seq desc limit 1")
    GridTimeWeeksEntity getLast();

    GridTimeWeeksEntity findByClockTime(LocalDateTime clockTime);
}

