package com.dreamscale.gridtime.core.domain.tile;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface GridBridgeMetricsRepository extends CrudRepository<GridBoxMetricsEntity, UUID> {

}
