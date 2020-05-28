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
public class TeamLinkDto {

    private UUID id;
    private String name;
    private String teamType;

}
