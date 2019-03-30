package com.dreamscale.ideaflow.api.team;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDto {

    private UUID organizationId;
    private UUID teamId;
    private UUID memberId;

    private String memberEmail;
    private String memberName;
    private String teamName;
}
