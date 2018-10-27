package com.dreamscale.htmflow.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamWithMembersDto {

    private UUID organizationId;
    private UUID teamId;
    private String teamName;

    private TeamMemberWorkStatusDto me;
    private List<TeamMemberWorkStatusDto> teamMembers;

    public void addMember(TeamMemberWorkStatusDto teamMember) {
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }

        teamMembers.add(teamMember);
    }
}
