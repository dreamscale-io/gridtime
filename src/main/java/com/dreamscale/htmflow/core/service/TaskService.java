package com.dreamscale.htmflow.core.service;

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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {

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

    public TaskDto findByTaskName(UUID organizationId, UUID projectId, String taskName) {
        //TODO make this case insensitive search, so toyot-123 and TOYOT-123 both work

        TaskEntity taskEntity = taskRepository.findByProjectIdAndName(projectId, taskName);
        return taskMapper.toApi(taskEntity);
    }

    public RecentTasksByProjectDto getRecentTasksByProjectForUser(UUID organizationId, UUID masterAccountId) {

        //TODO this depends on chunks that were entered, and tracking recent usage of things

        return new RecentTasksByProjectDto();
    }

    public TaskDto createNewTask(UUID organizationId, UUID projectId, UUID masterAccountId, TaskInputDto taskInputDto) {
        //TODO need to figure out how to post new tasks in Jira, and shift to in progress state with first chunk
        return new TaskDto();
    }
}