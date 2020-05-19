package com.dreamscale.gridtime.api.team;

import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamCircuitDto {

    private UUID organizationId;
    private UUID teamId;

    private String teamName;

    private Boolean isHomeTeam;

    TeamCircuitRoomDto defaultRoom;

    private UUID ownerId;
    private String ownerName;

    private UUID moderatedId;
    private String moderatorName;

    List<TeamMemberDto> teamMembers;

    List<TeamCircuitRoomDto> teamRooms;

}
