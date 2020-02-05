package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TeamCircuitRoomRepository extends CrudRepository<TeamCircuitRoomEntity, UUID> {

    TeamCircuitRoomEntity findByTeamIdAndLocalName(UUID teamId, String roomName);

    @Query(nativeQuery = true, value = "select tcr.* from team_circuit_room tcr, team t " +
            "where tcr.organization_id = (:organizationId) " +
            "and tcr.team_id = t.id "+
            "and tcr.local_name = (:roomName) "+
            "and t.name = (:teamName) ")
    TeamCircuitRoomEntity findByOrganizationIdTeamNameAndLocalName(
            @Param("organizationId") UUID organizationId,
            @Param("teamName")  String teamName,
            @Param("roomName") String roomName);
}
