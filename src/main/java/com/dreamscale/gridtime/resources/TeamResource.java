package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.OrganizationService;
import com.dreamscale.gridtime.core.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = ResourcePaths.TEAM_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class TeamResource {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TeamService teamService;

    /**
     * Get the active status of me
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping()
    public TeamDto getMyTeam() {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return teamService.getMyPrimaryTeam(invokingMember.getOrganizationId(), invokingMember.getId());
    }


}