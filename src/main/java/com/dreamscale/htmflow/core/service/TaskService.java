package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraNewTaskDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraNewTaskFields;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
@Service
public class TaskService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private JiraService jiraService;

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

    public TaskDto createNewTask(UUID organizationId, UUID projectId, UUID masterAccountId, TaskInputDto taskInputDto) {

        ProjectEntity project = projectRepository.findById(projectId);
        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndMasterAccountId(organizationId, masterAccountId);

        JiraTaskDto jiraTaskDto = jiraService.createNewTask(organizationId, project.getExternalId(), membership.getExternalId(), taskInputDto);

        TaskEntity taskEntity = new TaskEntity();
        taskEntity.setId(UUID.randomUUID());
        taskEntity.setName(jiraTaskDto.getKey());
        taskEntity.setSummary(jiraTaskDto.getSummary());
        taskEntity.setStatus(jiraTaskDto.getStatus());
        taskEntity.setExternalId(jiraTaskDto.getId());
        taskEntity.setProjectId(projectId);
        taskEntity.setOrganizationId(organizationId);

        taskRepository.save(taskEntity);

        return taskMapper.toApi(taskEntity);
    }
}