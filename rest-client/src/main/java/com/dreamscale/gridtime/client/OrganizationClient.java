package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface OrganizationClient {

    //find out about orgs you're participating in

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MY_PATH + ResourcePaths.ACTIVE_PATH)
    OrganizationDto getMyActiveOrganization();

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<OrganizationDto> getParticipatingOrganizations();

    //Get members of your organization

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH)
    List<MemberRegistrationDto> getOrganizationMembers();

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + "/{memberId}")
    MemberRegistrationDto getOrganizationMember(@Param("memberId") String memberId);

    //Must be organization *owner *to use these APIs

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + "/{memberId}" + ResourcePaths.REMOVE_PATH)
    SimpleStatusDto removeOrganizationMember(@Param("memberId") String memberId);

    @RequestLine("GET " +ResourcePaths.ORGANIZATION_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    JiraConfigDto getJiraConfiguration();

    @RequestLine("POST " +ResourcePaths.ORGANIZATION_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    SimpleStatusDto updateJiraConfiguration(JiraConfigDto jiraConfigDto);


    //Join an existing org using the invitation token

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.JOIN_PATH)
    SimpleStatusDto joinOrganizationWithInvitationAndEmail(JoinRequestInputDto joinRequestInputDto);

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.JOIN_PATH + ResourcePaths.EMAIL_PATH +
            ResourcePaths.VALIDATE_PATH + "?validationCode={validationCode}")
    SimpleStatusDto validateMemberEmailAndJoin(@Param("validationCode") String validationCode);

}
