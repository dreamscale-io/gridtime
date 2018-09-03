package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.core.domain.OrganizationEntity;
import com.dreamscale.htmflow.core.domain.OrganizationRepository;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnection;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
}
