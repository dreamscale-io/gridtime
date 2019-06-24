package com.dreamscale.htmflow.core.domain.time;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridTimeDayPartsRepository extends CrudRepository<GridTimeDayPartsEntity, UUID> {

    @Query(nativeQuery = true, value = "select * from grid_time_dayparts " +
            "order by tile_seq desc limit 1")
    GridTimeDayPartsEntity getLast();
}
