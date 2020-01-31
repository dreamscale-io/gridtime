package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamCircuitRoomRepository extends CrudRepository<TeamCircuitRoomEntity, UUID> {

    TeamCircuitRoomEntity findByTeamId(UUID teamId);
}
