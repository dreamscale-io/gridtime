package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.core.capability.journal.ProjectCapability;
import com.dreamscale.gridtime.core.capability.journal.TaskCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private ProjectCapability projectCapability;

    @Autowired
    private TaskCapability taskCapability;

    @Autowired
    private OrganizationCapability organizationCapability;


    /**
     * Retrieve all projects (public and private) for the organization that member has permission to see
     */
    @GetMapping()
    List<ProjectDto> getProjects() {
        RequestContext context = RequestContext.get();
        log.info("getProjects, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        return projectCapability.getAllProjectsWithPermission(membership.getOrganizationId(), membership.getId());
    }

    /**
     *  Retrieves a specific project and all related details
     *
     *  Must have permission to access the project
     * @param projectId
     * @return ProjectDetailsDto
     */
    @GetMapping("/{id}")
    ProjectDetailsDto getProjectDetails(@PathVariable("id") String projectId) {
        RequestContext context = RequestContext.get();
        log.info("getProjectDetails, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.getProjectDetails(membership.getOrganizationId(), membership.getId(), projectIdParsed);
    }

    /**
     * Updates the "box configuration" for the project.
     *
     * Whenever there is file activity within a "box" (an include/exclude path match expression)
     * Idea Flow Metrics will be summarized according to the specified boxes, and aggregated for the team.
     *
     * Your box configuration gives you a window into which areas of code
     * contain the most flow and friction within your project
     *
     * @param projectId
     * @param projectBoxConfiguration ProjectBoxConfigurationDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.BOX_PATH)
    SimpleStatusDto updateBoxConfiguration(@PathVariable("id") String projectId, @RequestBody ProjectBoxConfigurationInputDto projectBoxConfiguration) {
        RequestContext context = RequestContext.get();
        log.info("updateBoxConfiguration, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.updateBoxConfiguration(membership.getOrganizationId(), membership.getId(), projectIdParsed, projectBoxConfiguration);
    }

    /**
     * Grants access to the current project to a specific member or team within the organization.
     *
     * This will cause the project to show up for the user as an available project, and allow them to
     * add tasks, and contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param grantAccessInput GrantAccessInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.GRANT_PATH)
    SimpleStatusDto grantPermission(@PathVariable("id") String projectId, @RequestBody GrantAccessInputDto grantAccessInput) {
        RequestContext context = RequestContext.get();
        log.info("grantPermission, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.grantAccessForProject(membership.getOrganizationId(), membership.getId(), projectIdParsed, grantAccessInput);
    }

    /**
     * Revokes access to the current project for a specific member or team within the organization.
     *
     * This will cause the project to stop showing up for the user as an available project,
     * and removes the ability to add tasks, or contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param revokeAccessInput GrantAccessInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.REVOKE_PATH)
    SimpleStatusDto revokePermission(@PathVariable("id") String projectId, @RequestBody GrantAccessInputDto revokeAccessInput) {
        RequestContext context = RequestContext.get();
        log.info("revokePermission, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.revokeAccessForProject(membership.getOrganizationId(), membership.getId(), projectIdParsed, revokeAccessInput);
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

        return taskCapability.findTasksStartingWith(membership.getOrganizationId(), UUID.fromString(projectId), startsWith);
    }

    private UUID getActiveOrgId() {
        RequestContext context = RequestContext.get();
        OrganizationDto org = organizationCapability.getActiveOrganization(context.getRootAccountId());
        return org.getId();
    }

}
