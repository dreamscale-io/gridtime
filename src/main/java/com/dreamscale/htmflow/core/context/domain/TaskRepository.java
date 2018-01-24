package com.dreamscale.htmflow.core.context.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskEntity, UUID> {
}
