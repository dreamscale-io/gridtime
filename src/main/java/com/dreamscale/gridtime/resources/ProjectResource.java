package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
import com.dreamscale.gridtime.api.project.ProjectBoxConfigurationInputDto;
import com.dreamscale.gridtime.api.project.ProjectDetailsDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.team.TeamInputDto;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.api.terminal.ActivityContext;
import com.dreamscale.gridtime.core.capability.journal.ProjectCapability;
import com.dreamscale.gridtime.core.capability.journal.TaskCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRoute;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {


    @Autowired
    private ProjectCapability projectCapability;

    @Autowired
    private TaskCapability taskCapability;

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private TerminalRouteRegistry terminalRouteRegistry;

    @PostConstruct
    void init() {
        terminalRouteRegistry.register(ActivityContext.PROJECT, Command.SHARE,
                "Share private projects with individuals and teams.",
                new ShareProjectWithUserTerminalRoute(), new ShareProjectWithTeamTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.PROJECT, Command.UNSHARE,
                "Unshare previously shared projects and revoke access for individuals and teams.",
                new UnshareProjectForUserTerminalRoute(), new UnshareProjectForTeamTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.PROJECT, Command.VIEW,
                "View the current details and permissions configured for a project",
                new ViewProjectTerminalRoute());

    }

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
     * Grants access to the current project to a specific member within the organization.
     *
     * This will cause the project to show up for the user as an available project, and allow them to
     * add tasks, and contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param usernameInputDto UsernameInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.GRANT_PATH + ResourcePaths.USERNAME_PATH)
    SimpleStatusDto grantPermissionToUser(@PathVariable("id") String projectId, @RequestBody UsernameInputDto usernameInputDto) {
        RequestContext context = RequestContext.get();
        log.info("grantPermissionToUser, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.grantAccessForMember(membership.getOrganizationId(), membership.getId(), projectIdParsed, usernameInputDto.getUsername());
    }

    /**
     * Revokes access to the current project for a specific member within the organization.
     *
     * This will cause the project to stop showing up for the user as an available project,
     * and removes the ability to add tasks, or contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param usernameInputDto UsernameInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.REVOKE_PATH + ResourcePaths.USERNAME_PATH)
    SimpleStatusDto revokePermissionForUser(@PathVariable("id") String projectId, @RequestBody UsernameInputDto usernameInputDto) {
        RequestContext context = RequestContext.get();
        log.info("revokePermissionFromUser, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.revokeAccessForMember(membership.getOrganizationId(), membership.getId(), projectIdParsed, usernameInputDto.getUsername());
    }


    /**
     * Grants access to the current project to a specific team within the organization.
     *
     * This will cause the project to show up for all users of the team as an available project, and allow them to
     * add tasks, and contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param teamInputDto TeamInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.GRANT_PATH + ResourcePaths.TEAM_PATH)
    SimpleStatusDto grantPermissionToTeam(@PathVariable("id") String projectId, @RequestBody TeamInputDto teamInputDto) {
        RequestContext context = RequestContext.get();
        log.info("grantPermissionToTeam, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.grantAccessForTeam(membership.getOrganizationId(), membership.getId(), projectIdParsed, teamInputDto.getName());
    }

    /**
     * Revokes access to the current project for a specific team within the organization.
     *
     * This will cause the project to stop showing up for all users of the team as an available project,
     * and removes the ability to add tasks, or contribute Idea Flow data to the project metrics.
     *
     * @param projectId
     * @param teamInputDto TeamInputDto
     * @return SimpleStatusDto
     */
    @PostMapping("/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.REVOKE_PATH + ResourcePaths.TEAM_PATH)
    SimpleStatusDto revokePermissionForTeam(@PathVariable("id") String projectId, @RequestBody TeamInputDto teamInputDto) {
        RequestContext context = RequestContext.get();
        log.info("revokePermissionForTeam, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        UUID projectIdParsed = UUID.fromString(projectId);

        return projectCapability.revokeAccessForTeam(membership.getOrganizationId(), membership.getId(), projectIdParsed,  teamInputDto.getName());
    }

    /**
     * Autocomplete search finds the top 10 tasks with a name that starts with the provided search string.
     * Search must include a number, so search for FP-1 will return results, whereas searching for FP is a BadRequest
     *
     * @param projectId
     * @param startsWith
     * @return List<TaskDto>
     */
    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.SEARCH_PATH + "/{startsWith}")
    List<TaskDto> findTasksStartingWith(@PathVariable("id") String projectId, @PathVariable("startsWith") String startsWith) {
        RequestContext context = RequestContext.get();
        log.info("findTasksStartingWith, user={}", context.getRootAccountId());

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        return taskCapability.findTasksStartingWith(membership.getOrganizationId(), UUID.fromString(projectId), startsWith);
    }

    private UUID getProjectId(String projectName) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity membership = organizationCapability.getActiveMembership(context.getRootAccountId());

        ProjectDto project = projectCapability.getProjectByName(membership.getOrganizationId(), membership.getId(), projectName);
        return project.getId();
    }


    private class ShareProjectWithUserTerminalRoute extends TerminalRoute {

        private static final String PROJECT_NAME_PARAM = "projectName";
        private static final String USERNAME_PARAM = "username";

        ShareProjectWithUserTerminalRoute() {
            super(Command.SHARE, "project {" + PROJECT_NAME_PARAM + "} with user {" + USERNAME_PARAM + "}");

            describeTextOption(PROJECT_NAME_PARAM, "the name of the project to grant access");
            describeTextOption(USERNAME_PARAM, "the user to receive access");
        }

        @Override
        public Object route(Map<String, String> params) {
            String projectName = params.get(PROJECT_NAME_PARAM);
            String username = params.get(USERNAME_PARAM);

            UUID projectId = getProjectId(projectName);

            return grantPermissionToUser(projectId.toString(), new UsernameInputDto(username));
        }
    }

    private class ShareProjectWithTeamTerminalRoute extends TerminalRoute {

        private static final String PROJECT_NAME_PARAM = "projectName";
        private static final String TEAM_NAME_PARAM = "teamName";

        ShareProjectWithTeamTerminalRoute() {
            super(Command.SHARE, "project {" + PROJECT_NAME_PARAM + "} with team {" + TEAM_NAME_PARAM + "}");

            describeTextOption(PROJECT_NAME_PARAM, "the name of the project to grant access");
            describeTextOption(TEAM_NAME_PARAM, "the team to receive access");
        }

        @Override
        public Object route(Map<String, String> params) {
            String projectName = params.get(PROJECT_NAME_PARAM);
            String teamName = params.get(TEAM_NAME_PARAM);

            UUID projectId = getProjectId(projectName);

            return grantPermissionToTeam(projectId.toString(), new TeamInputDto(teamName));
        }
    }

    private class UnshareProjectForUserTerminalRoute extends TerminalRoute {

        private static final String PROJECT_NAME_PARAM = "projectName";
        private static final String USERNAME_PARAM = "username";

        UnshareProjectForUserTerminalRoute() {
            super(Command.UNSHARE, "project {" + PROJECT_NAME_PARAM + "} for user {" + USERNAME_PARAM + "}");

            describeTextOption(PROJECT_NAME_PARAM, "the name of the project to revoke access from");
            describeTextOption(USERNAME_PARAM, "the user to revoke access from");
        }

        @Override
        public Object route(Map<String, String> params) {
            String projectName = params.get(PROJECT_NAME_PARAM);
            String username = params.get(USERNAME_PARAM);

            UUID projectId = getProjectId(projectName);

            return revokePermissionForUser(projectId.toString(), new UsernameInputDto(username));
        }
    }

    private class UnshareProjectForTeamTerminalRoute extends TerminalRoute {

        private static final String PROJECT_NAME_PARAM = "projectName";
        private static final String TEAM_NAME_PARAM = "teamName";

        UnshareProjectForTeamTerminalRoute() {
            super(Command.UNSHARE, "project {" + PROJECT_NAME_PARAM + "} for team {" + TEAM_NAME_PARAM + "}");

            describeTextOption(PROJECT_NAME_PARAM, "the name of the project to revoke access from");
            describeTextOption(TEAM_NAME_PARAM, "the team to revoke access from");
        }

        @Override
        public Object route(Map<String, String> params) {
            String projectName = params.get(PROJECT_NAME_PARAM);
            String teamName = params.get(TEAM_NAME_PARAM);

            UUID projectId = getProjectId(projectName);

            return revokePermissionForTeam(projectId.toString(), new TeamInputDto(teamName));
        }
    }

    private class ViewProjectTerminalRoute extends TerminalRoute {

        private static final String PROJECT_NAME_PARAM = "projectName";

        ViewProjectTerminalRoute() {
            super(Command.VIEW, "project {" + PROJECT_NAME_PARAM + "}");

            describeTextOption(PROJECT_NAME_PARAM, "the name of the project to view");
        }

        @Override
        public Object route(Map<String, String> params) {
            String projectName = params.get(PROJECT_NAME_PARAM);

            UUID projectId = getProjectId(projectName);

            return getProjectDetails(projectId.toString());
        }
    }

}
