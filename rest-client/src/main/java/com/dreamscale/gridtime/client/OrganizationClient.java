package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
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


    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH)
    OrganizationDto getMyActiveOrganization();

    //Get members of your organization

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH)
    List<MemberDetailsDto> getOrganizationMembers();

    //Must be organization *owner *to use these APIs

    @RequestLine("POST " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MEMBER_PATH + "/{memberId}" + ResourcePaths.REMOVE_PATH)
    SimpleStatusDto removeMember(@Param("memberId") String memberId);

    @RequestLine("GET " +ResourcePaths.ORGANIZATION_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    JiraConfigDto getJiraConfiguration();

    @RequestLine("POST " +ResourcePaths.ORGANIZATION_PATH + ResourcePaths.CONFIG_PATH + ResourcePaths.JIRA_PATH)
    SimpleStatusDto updateJiraConfiguration(JiraConfigDto jiraConfigDto);

    @RequestLine("GET " + ResourcePaths.ORGANIZATION_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<OrganizationDto> getParticipatingOrganizations();
}
