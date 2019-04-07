package com.dreamscale.htmflow.core.domain.active;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ActiveWorkStatusRepository extends CrudRepository<ActiveWorkStatusEntity, UUID> {

    ActiveWorkStatusEntity findById(UUID id);

    ActiveWorkStatusEntity findByMemberId(UUID memberId);
}
