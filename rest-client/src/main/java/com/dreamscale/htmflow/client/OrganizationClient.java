package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
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

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.MEMBER_PATH)
    List<OrgMemberStatusDto> getMembers(@Param("id") String organizationId);
}
