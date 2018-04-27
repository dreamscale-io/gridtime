package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.account.ActivationTokenDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.organization.MembershipDto;
import com.dreamscale.htmflow.api.organization.MembershipInputDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.organization.OrganizationInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface OrganizationClient {

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH)
    OrganizationDto createOrganization(OrganizationInputDto organizationInputDto);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + ResourcePaths.INVITATION_PATH
            + "?token={token}")
    OrganizationDto getMemberInvitation(@Param("token") String inviteToken);

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH+ "/{id}"  + ResourcePaths.MEMBER_PATH)
    MembershipDto registerMember(@Param("id") String organizationId, MembershipInputDto membershipInputDto);

}
