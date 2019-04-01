package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.api.status.ConnectionResultDto;
import com.dreamscale.htmflow.api.status.Status;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
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

    public JiraSearchResultPage getOpenTasksForProject(UUID organizationId, String externalProjectId, int startAt, int maxResults) {
        OrganizationEntity orgEntity = organizationRepository.findById(organizationId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());

        return connection.getOpenTasksForProject(externalProjectId, startAt, maxResults);
    }

    public List<JiraTaskDto> getOpenTasksForProject(UUID organizationId, String externalProjectId) {
        OrganizationEntity orgEntity = organizationRepository.findById(organizationId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());

        int startAt = 0;
        int maxResults = 100;

        JiraSearchResultPage page = connection.getOpenTasksForProject(externalProjectId, startAt, maxResults);
        log.info("Fetching "+page.getTotal() + " total tasks");

        List<JiraTaskDto> allTasks = new ArrayList<>();
        allTasks.addAll(page.getIssues());

        while (page.getTotal() > startAt + maxResults) {
            startAt += maxResults;
            page = connection.getOpenTasksForProject(externalProjectId, startAt, maxResults);
            allTasks.addAll(page.getIssues());
        }

        return allTasks;
    }



    public JiraTaskDto createNewTask(UUID organizationId, String externalProjectId, String externalUserKey, TaskInputDto taskInputDto) {

        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        JiraUserDto jiraUserDto = connection.getUserByKey(externalUserKey);

        if (jiraUserDto == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_JIRA_USER, "Jira user not found");
        }

        JiraNewTaskDto newTaskDto = new JiraNewTaskDto(taskInputDto.getSummary(),
                taskInputDto.getDescription(),
                externalProjectId,
                "Task");

        JiraTaskDto newTask = connection.createTask(newTaskDto);

        connection.updateTransition(newTask.getKey(), "In Progress");
        connection.updateAssignee(newTask.getKey(), externalUserKey);

        return connection.getTask(newTask.getKey());
    }

    public JiraTaskDto getTask(UUID organizationId, String taskKey) {
        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        return connection.getTask(taskKey);
    }

    public JiraTaskDto closeTask(UUID organizationId, String taskKey) {
        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        connection.updateTransition(taskKey, "Done");

        return connection.getTask(taskKey);
    }

    public void deleteTask(UUID organizationId, String taskKey) {
        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection connection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        connection.deleteTask(taskKey);
    }


    public JiraUserDto getUserByEmail(UUID organizationId, String email) {

        OrganizationEntity org = organizationRepository.findById(organizationId);
        if (org == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraConnection jiraConnection = jiraConnectionFactory.connect(org.getJiraSiteUrl(), org.getJiraUser(), org.getJiraApiKey());

        JiraUserDto selectedUser = jiraConnection.getUserByEmail(email);

        if (selectedUser == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_JIRA_USER, "Jira User not found: "+email);
        }

        return selectedUser;
    }

    public ConnectionResultDto validateJiraConnection(OrganizationEntity orgEntity) {
        ConnectionResultDto result = new ConnectionResultDto();

        if (jiraUserNotInOrgDomain(orgEntity.getDomainName(), orgEntity.getJiraUser())) {
            result.setStatus(Status.FAILED);
            result.setMessage("Jira user not in organization domain");
        } else {
            try {
                JiraConnection connection = jiraConnectionFactory.connect(orgEntity.getJiraSiteUrl(), orgEntity.getJiraUser(), orgEntity.getJiraApiKey());
                connection.validate();
                result.setStatus(Status.VALID);
            } catch (Exception ex) {
                result.setStatus(Status.FAILED);
                result.setMessage("Failed to connect to Jira");
            }
        }

        return result;
    }

    private boolean jiraUserNotInOrgDomain(String domainName, String jiraUser) {
        return domainName == null || jiraUser == null || !jiraUser.toLowerCase().endsWith(domainName.toLowerCase());
    }
}
