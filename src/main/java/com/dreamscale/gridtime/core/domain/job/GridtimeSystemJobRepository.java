package com.dreamscale.gridtime.core.domain.job;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridtimeSystemJobRepository extends CrudRepository<GridtimeSystemJobEntity, UUID> {

}
