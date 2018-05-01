package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ActiveWorkStatusRepository extends CrudRepository<ActiveWorkStatusEntity, UUID> {

    ActiveWorkStatusEntity findById(UUID id);
}
