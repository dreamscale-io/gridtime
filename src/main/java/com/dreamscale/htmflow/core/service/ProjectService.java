package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.JiraUserDto;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ErrorEntity;
import org.dreamscale.exception.WebApplicationException;
import org.dreamscale.logging.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProjectService {

    @Autowired
    private JiraConnectionFactory jiraConnectionFactory;

    @Autowired
    private JiraSyncService jiraSyncService;

    @Autowired
    private OrganizationRepository organizationRepository;

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

    public List<ProjectDto> getAllProjects(UUID organizationId) {

        JiraConnection jiraConnection = connectToOrgJira(organizationId);
        jiraSyncService.synchronizeProjectsWithJira(organizationId, jiraConnection);

        Iterable<ProjectEntity> projectEntities = projectRepository.findAll();
        return projectMapper.toApiList(projectEntities);
    }

    public List<TaskDto> getAllTasksForProject(UUID organizationId, UUID projectId) {

        JiraConnection jiraConnection = connectToOrgJira(organizationId);
        jiraSyncService.synchronizeOpenTasksWithJira(organizationId, projectId, jiraConnection);

        Iterable<TaskEntity> taskEntities = taskRepository.findByProjectId(projectId);
        return taskMapper.toApiList(taskEntities);

    }

    private JiraConnection connectToOrgJira(UUID organizationId) {
        OrganizationEntity org = organizationRepository.findById(organizationId);

        return jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());
    }

}