package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TorchieTombstoneRepository extends CrudRepository<TorchieTombstoneEntity, UUID> {


    @Query(nativeQuery = true, value = "select * from torchie_tombstone t " +
            "where spirit_id=(:spiritId) " +
            "order by date_of_death desc limit 1")
    TorchieTombstoneEntity findLatestBySpiritId(@Param("spiritId") UUID spiritId);

    List<TorchieTombstoneEntity> findBySpiritIdOrderByDateOfDeath(UUID spiritId);
}
