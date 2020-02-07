package com.dreamscale.gridtime.core.domain.active;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ActiveAccountStatusRepository extends CrudRepository<ActiveAccountStatusEntity, UUID> {

    ActiveAccountStatusEntity findByRootAccountId(UUID id);

    ActiveAccountStatusEntity findByConnectionId(UUID connectionId);




}
