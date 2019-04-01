package com.dreamscale.htmflow.core.domain;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActiveSpiritLinkRepository extends CrudRepository<ActiveSpiritLinkEntity, UUID> {

    List<ActiveSpiritLinkEntity> findBySpiritId(UUID spiritId);

    @Query(nativeQuery = true, value = "select * from active_spirit_link net " +
            "where exists (select 1 from active_spirit_link me " +
            "where me.spirit_id = (:spiritId) " +
            "and me.network_id = net.network_id) ")
    List<ActiveSpiritLinkEntity> findMySpiritNetwork(@Param("spiritId") UUID spiritId);

    ActiveSpiritLinkEntity findByNetworkIdAndSpiritId(UUID networkId, UUID spiritId);

    List<ActiveSpiritLinkEntity> findByNetworkId(UUID networkId);
}
