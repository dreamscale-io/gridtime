package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GridTileCarryOverContextRepository extends CrudRepository<GridTileCarryOverContextEntity, UUID> {

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_tile_carry_over_context")
    void truncate();

    @Modifying
    @Query(nativeQuery = true, value = "delete from grid_tile_carry_over_context where torchie_id=(:torchieId) and calendar_id=(:calendarId)")
    void deleteByTorchieIdAndCalendarId(@Param("torchieId") UUID torchieId, @Param("calendarId") UUID calendarId);

    GridTileCarryOverContextEntity findByTorchieIdAndCalendarId(UUID torchieId, UUID calendarId);
}
