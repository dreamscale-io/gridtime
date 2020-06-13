package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamLinkDto;
import com.dreamscale.gridtime.core.capability.journal.TeamProjectCapability;
import com.dreamscale.gridtime.core.capability.journal.TeamTaskCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private TeamProjectCapability teamProjectCapability;

    @Autowired
    private TeamTaskCapability teamTaskCapability;

    @Autowired
    private OrganizationCapability organizationCapability;


    /**
     * Retrieve all projects for the organization
     */
    @GetMapping()
    List<ProjectDto> getAllTeamProjects() {
        RequestContext context = RequestContext.get();
        log.info("getAllTeamProjects, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        TeamLinkDto teamLink = teamCapability.getMyActiveTeamLink(membership.getOrganizationId(), membership.getId());

        return teamProjectCapability.getAllTeamProjects(membership.getOrganizationId(), teamLink.getId(), membership.getId());
    }

    /**
     * Autocomplete search finds the top 10 tasks with a name that starts with the provided search string.
     * Search must include a number, so search for FP-1 will return results, whereas searching for FP is a BadRequest
     */
    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.SEARCH_PATH + "/{startsWith}")
    List<TaskDto> findTasksStartingWith(@PathVariable("id") String projectId, @PathVariable("startsWith") String startsWith) {
        RequestContext context = RequestContext.get();
        log.info("findTasksStartingWith, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        return teamTaskCapability.findTasksStartingWith(membership.getOrganizationId(), UUID.fromString(projectId), startsWith);
    }

    private UUID getActiveOrgId() {
        RequestContext context = RequestContext.get();
        OrganizationDto org = organizationCapability.getActiveOrganization(context.getRootAccountId());
        return org.getId();
    }

}
