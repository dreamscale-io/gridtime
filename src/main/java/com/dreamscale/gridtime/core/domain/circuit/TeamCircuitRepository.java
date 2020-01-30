package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamCircuitRepository extends CrudRepository<TeamCircuitEntity, UUID> {

    TeamCircuitEntity findByTeamId(UUID teamId);
}
