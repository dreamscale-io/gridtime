package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface GridMarkerRepository extends CrudRepository<GridMarkerEntity, UUID> {

    List<GridMarkerEntity> findByTorchieIdAndRowName(UUID torchieId, String rowName);

    List<GridMarkerEntity> findByTorchieId(UUID torchieId);

    @Modifying
    @Query(nativeQuery = true, value = "truncate table grid_marker")
    void truncate();
}
