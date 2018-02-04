package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.ProjectInputDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.PROJECT_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ProjectResource {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    @GetMapping()
    List<ProjectDto> getProjects() {
        Iterable<ProjectEntity> projectEntities = projectRepository.findAll();
        return projectMapper.toApiList(projectEntities);
    }

    @GetMapping("/{id}" + ResourcePaths.TASK_PATH)
    List<TaskDto> getOpenTasksForProject(@PathVariable("id") String projectId) {
        Iterable<TaskEntity> taskEntities = taskRepository.findAll();
        return taskMapper.toApiList(taskEntities);
    }

    @GetMapping(ResourcePaths.RECENT_PATH)
    List<ProjectDto> getRecentProjects() {
        Iterable<ProjectEntity> projectEntities = projectRepository.findAll();
        return projectMapper.toApiList(projectEntities);
    }

    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.RECENT_PATH)
    List<TaskDto> getRecentTasksForProject(@PathVariable("id") String projectId) {
        Iterable<TaskEntity> taskEntities = taskRepository.findAll();
        return taskMapper.toApiList(taskEntities);
    }

    @GetMapping("/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.NAME_PATH + "/{name}")
    TaskDto getTaskByName(@PathVariable("id") String projectId, @PathVariable("name") String taskName) {
        return new TaskDto();
    }

    @PostMapping()
    ProjectDto createProject(@RequestBody ProjectInputDto projectInputDto) {
        return new ProjectDto();
    }

    @PostMapping("/{id}" + ResourcePaths.TASK_PATH)
    TaskDto createTask(@RequestBody TaskInputDto taskInputDto) {
        return new TaskDto();
    }

}
