package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.api.team.TeamInputDto;
import com.dreamscale.htmflow.api.team.TeamMemberDto;
import com.dreamscale.htmflow.api.team.TeamMembersToAddInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;
import java.util.UUID;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface OrganizationClient {

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH)
    OrganizationDto createOrganization(OrganizationInputDto organizationInputDto);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH
            + "?token={token}")
    OrganizationDto decodeInvitation(@Param("token") String inviteToken);

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.MEMBER_PATH)
    MemberRegistrationDetailsDto registerMember(@Param("id") String organizationId, MembershipInputDto membershipInputDto);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.MEMBER_PATH)
    List<OrgMemberStatusDto> getMembers(@Param("id") String organizationId);

    @RequestLine("POST "+ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.TEAM_PATH)
    TeamDto createTeam(@Param("id") String organizationId, TeamInputDto teamInputDto);

    @RequestLine("POST "+ResourcePaths.ORGANIZATION_PATH + "/{orgId}"  + ResourcePaths.TEAM_PATH  + "/{id}"
            + ResourcePaths.MEMBER_PATH)
    List<TeamMemberDto> addMembersToTeam(@Param("orgId") String organizationId, @Param("id") String teamId,
                                         TeamMembersToAddInputDto teamMemberInputDto);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.TEAM_PATH)
    List<TeamDto> getTeams(@Param("id") String organizationId);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.TEAM_PATH + ResourcePaths.ME_PATH)
    List<TeamDto> getMyTeams(@Param("id") String organizationId);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.TEAM_PATH + "/{teamId}" + ResourcePaths.MEMBER_PATH)
    List<TeamMemberWorkStatusDto> getStatusOfTeamMembers(@Param("id") String organizationId, @Param("teamId") String teamId);



}
