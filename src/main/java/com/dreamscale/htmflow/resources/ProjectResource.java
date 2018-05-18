package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.ProjectService;
import com.dreamscale.htmflow.core.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private TaskRepository taskRepository;


    @GetMapping()
    List<ProjectDto> getAllProjects() {

        return projectService.getAllProjects(getDefaultOrgId());
    }

    @GetMapping("/{id}" + ResourcePaths.TASK_PATH)
    List<TaskDto> getAllOpenTasksForProject(@PathVariable("id") String projectId) {

        return projectService.getAllTasksForProject(getDefaultOrgId(), UUID.fromString(projectId));
    }

    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.NAME_PATH + "/{name}")
    TaskDto findTaskByName(@PathVariable("id") String projectId, @PathVariable("name") String taskName) {

        return taskService.findByTaskName(getDefaultOrgId(), UUID.fromString(projectId), taskName);
    }

    @GetMapping(ResourcePaths.TASK_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksByProjectDto getRecentTasksByProjectForUser() {
        RequestContext context = RequestContext.get();

        return taskService.getRecentTasksByProjectForUser(getDefaultOrgId(), context.getMasterAccountId());
    }

    @PostMapping("/{id}" + ResourcePaths.TASK_PATH)
    TaskDto createNewTask(@PathVariable("id") String projectId, @RequestBody TaskInputDto taskInputDto) {
        RequestContext context = RequestContext.get();
        return taskService.createNewTask(getDefaultOrgId(), UUID.fromString(projectId), context.getMasterAccountId(), taskInputDto);
    }

    private UUID getDefaultOrgId() {
        RequestContext context = RequestContext.get();
        OrganizationDto org = organizationService.getDefaultOrganization(context.getMasterAccountId());
        return org.getId();
    }

}
