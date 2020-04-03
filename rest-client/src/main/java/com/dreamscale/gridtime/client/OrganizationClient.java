package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamInputDto;
import com.dreamscale.gridtime.api.team.TeamMemberDto;
import com.dreamscale.gridtime.api.team.TeamMembersToAddInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

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


}
