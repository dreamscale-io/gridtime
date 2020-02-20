package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.project.TaskInputDto;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.directory.OrganizationDirectoryCapability;
import com.dreamscale.gridtime.core.service.ProjectService;
import com.dreamscale.gridtime.core.capability.directory.TaskDirectoryCapability;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskDirectoryCapability taskDirectoryCapability;

    @Autowired
    private OrganizationDirectoryCapability organizationDirectoryCapability;


    /**
     * Retrieve all projects for the organization
     */
    @GetMapping()
    List<ProjectDto> getAllProjects() {
        return projectService.getAllProjects(getDefaultOrgId());
    }

    /**
     * Autocomplete search finds the top 10 tasks with a name that starts with the provided search string.
     * Search must include a number, so search for FP-1 will return results, whereas searching for FP is a BadRequest
     */
    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.SEARCH_PATH + "/{startsWith}")
    List<TaskDto> findTasksStartingWith(@PathVariable("id") String projectId, @PathVariable("startsWith") String startsWith) {

        return taskDirectoryCapability.findTasksStartingWith(getDefaultOrgId(), UUID.fromString(projectId), startsWith);
    }

    /**
     * Creates a new task in Jira, assigns it to the user, and moves the state to In Progress.
     * The returned TaskDto includes the information for the newly created task.
     */
    @PostMapping("/{id}" + ResourcePaths.TASK_PATH)
    TaskDto createNewTask(@PathVariable("id") String projectId, @RequestBody TaskInputDto taskInputDto) {
        RequestContext context = RequestContext.get();
        return taskDirectoryCapability.createNewTask(getDefaultOrgId(), UUID.fromString(projectId), context.getRootAccountId(), taskInputDto);
    }

    private UUID getDefaultOrgId() {
        RequestContext context = RequestContext.get();
        OrganizationDto org = organizationDirectoryCapability.getDefaultOrganization(context.getRootAccountId());
        return org.getId();
    }

}
