package com.dreamscale.gridtime.core.domain.active;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ActiveSpiritLinkRepository extends CrudRepository<ActiveSpiritLinkEntity, UUID> {

    List<ActiveSpiritLinkEntity> findByTorchieId(UUID torchieId);

    @Query(nativeQuery = true, value = "select * from active_spirit_link net " +
            "where exists (select 1 from active_spirit_link me " +
            "where me.torchie_id = (:torchieId) " +
            "and me.network_id = net.network_id) ")
    List<ActiveSpiritLinkEntity> findMySpiritNetwork(@Param("torchieId") UUID torchieId);

    ActiveSpiritLinkEntity findByNetworkIdAndTorchieId(UUID networkId, UUID torchieId);

    List<ActiveSpiritLinkEntity> findByNetworkId(UUID networkId);
}
