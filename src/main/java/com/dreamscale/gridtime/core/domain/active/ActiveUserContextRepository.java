package com.dreamscale.gridtime.core.domain.active;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ActiveUserContextRepository extends CrudRepository<ActiveUserContextEntity, UUID> {

    ActiveUserContextEntity findByMasterAccountId(UUID masterAccountId);
}
