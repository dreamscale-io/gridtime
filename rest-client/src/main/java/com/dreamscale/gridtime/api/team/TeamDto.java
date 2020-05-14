package com.dreamscale.gridtime.api.team;

import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDto {

    private UUID organizationId;
    private UUID id;
    private String name;

    private TeamMemberDto me;
    private List<TeamMemberDto> teamMembers;

    private boolean isHomeTeam;

    public void addMember(TeamMemberDto teamMember) {
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }

        teamMembers.add(teamMember);
    }

}
