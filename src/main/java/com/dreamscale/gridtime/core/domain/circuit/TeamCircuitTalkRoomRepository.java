package com.dreamscale.gridtime.core.domain.circuit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface TeamCircuitTalkRoomRepository extends CrudRepository<TeamCircuitTalkRoomEntity, UUID> {


    TeamCircuitTalkRoomEntity findByOrganizationIdAndTeamNameAndCircuitRoomName(UUID organizationId, String teamName, String roomName);
}
