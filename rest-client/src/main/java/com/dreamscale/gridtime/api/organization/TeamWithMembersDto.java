package com.dreamscale.gridtime.api.organization;

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
public class TeamWithMembersDto {

    private UUID organizationId;
    private UUID teamId;
    private String teamName;

    private MemberWorkStatusDto me;
    private List<MemberWorkStatusDto> teamMembers;

    public void addMember(MemberWorkStatusDto teamMember) {
        if (teamMembers == null) {
            teamMembers = new ArrayList<>();
        }

        teamMembers.add(teamMember);
    }
}
