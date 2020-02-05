package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TeamCircuitTalkRoomRepository extends CrudRepository<TeamCircuitTalkRoomEntity, UUID> {


    TeamCircuitTalkRoomEntity findByOrganizationIdAndTeamNameAndCircuitRoomName(UUID organizationId, String teamName, String roomName);


    @Query(nativeQuery = true, value = "select * from team_circuit_talk_room_view  " +
            "where organization_id = (:organizationId) " +
            "and team_id = (:teamId) "+
            "and circuit_status = 'ACTIVE' "+
            "order by circuit_room_name ")
    List<TeamCircuitTalkRoomEntity> findByOrganizationIdAndTeamId(UUID organizationId, UUID teamId);
}
