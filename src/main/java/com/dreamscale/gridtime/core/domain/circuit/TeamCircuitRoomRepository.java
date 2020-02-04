package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamCircuitRoomRepository extends CrudRepository<TeamCircuitRoomEntity, UUID> {

    TeamCircuitRoomEntity findByTeamIdAndLocalName(UUID teamId, String roomName);

    @Query(nativeQuery = true, value = "select tcr.* from team_circuit_room tcr, team t " +
            "where tcr.organization_id = (:organizationId) " +
            "and tcr.team_id = t.id "+
            "and tcr.local_name = (:localName) "+
            "and t.name = (:teamName) ")
    TeamCircuitRoomEntity findByOrganizationIdTeamNameAndLocalName(UUID organizationId, String teamName, String roomName);
}
