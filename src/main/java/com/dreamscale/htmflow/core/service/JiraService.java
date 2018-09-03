package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraNewTaskDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraTaskDto;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraUserDto;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class JiraService {

    @Autowired
    OrganizationRepository organizationRepository;

    @Autowired
    JiraConnectionFactory jiraConnectionFactory;


    public JiraProjectDto getProjectByName(UUID organizationId, String projectName) {

        OrganizationEntity orgEntity = organizationRepository.findById(organizationId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());
        List<JiraProjectDto> projects = connection.getProjects();

        JiraProjectDto projectFound = null;

        for (JiraProjectDto project : projects) {
            if (project.getName().equalsIgnoreCase(projectName)) {
                projectFound = project;
            }
        }

        if (projectFound == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_JIRA_PROJECT, "Jira project not found");
        }

        return projectFound;
    }

    public List<JiraTaskDto> getOpenTasksForProject(UUID organizationId, String externalProjectId) {
        OrganizationEntity orgEntity = organizationRepository.findById(organizationId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());

        return connection.getOpenTasksForProject(externalProjectId);
    }

    public List<JiraProjectDto> getFilteredProjects(UUID organizationId, List<String> externalProjectIds) {
        OrganizationEntity orgEntity = organizationRepository.findById(organizationId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());
        List<JiraProjectDto> projects = connection.getProjects();

        List<JiraProjectDto> projectsFound = new ArrayList<>();

        for (JiraProjectDto project : projects) {
            if (externalProjectIds.contains(project.getId())) {
                projectsFound.add(project);
            }
        }

        return projectsFound;
    }

    public JiraTaskDto createNewTask(UUID organizationId, String externalProjectId, String externalUserId, TaskInputDto taskInputDto) {

        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        JiraUserDto jiraUserDto = connection.getUserByKey(externalUserId);

        if (jiraUserDto == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_JIRA_USER, "Jira user not found");
        }

        JiraNewTaskDto newTaskDto = new JiraNewTaskDto(taskInputDto.getSummary(),
                taskInputDto.getDescription(),
                externalProjectId,
                "Task");

        JiraTaskDto newTask = connection.createTask(newTaskDto);

        connection.updateTransition(newTask.getKey(), "In Progress");
        connection.updateAssignee(newTask.getKey(), externalUserId);

        return newTask;
    }
}
