package com.dreamscale.ideaflow.core.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TaskSwitchEventRepository extends CrudRepository<TaskSwitchEventEntity, UUID> {

}
