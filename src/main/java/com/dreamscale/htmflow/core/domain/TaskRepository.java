package com.dreamscale.htmflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends CrudRepository<TaskEntity, UUID> {

    List<TaskEntity> findByProjectId(UUID projectId);

    TaskEntity findByProjectIdAndName(UUID projectId, String taskName);
}
