package com.dreamscale.gridtime.core.domain.journal;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TaskSwitchEventRepository extends CrudRepository<TaskSwitchEventEntity, UUID> {

}
