package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.core.domain.ConfigProjectSyncEntity;
import com.dreamscale.htmflow.core.domain.ConfigProjectSyncRepository;
import com.dreamscale.htmflow.core.domain.OrganizationEntity;
import com.dreamscale.htmflow.core.domain.OrganizationRepository;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.hooks.jira.dto.JiraProjectDto;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private JiraSyncService jiraSyncService;

    @Autowired
    private ConfigProjectSyncRepository configProjectSyncRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectSyncOutputDto, ConfigProjectSyncEntity> projectSyncMapper;

    @PostConstruct
    private void init() {
        projectSyncMapper = mapperFactory.createDtoEntityMapper(ProjectSyncOutputDto.class, ConfigProjectSyncEntity.class);
    }

    public ProjectSyncOutputDto configureJiraProjectSync(ProjectSyncInputDto projectSyncDto) {

        OrganizationEntity organizationEntity = organizationRepository.findById(projectSyncDto.getOrganizationId());
        if (organizationEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraProjectDto jiraProject = jiraService.getProjectByName(organizationEntity.getId(), projectSyncDto.getProjectName());
        if (jiraProject == null) {
            throw  new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_JIRA_PROJECT, "Jira project not found");
        }

        ConfigProjectSyncEntity existingEntity = configProjectSyncRepository.findByOrganizationIdAndProjectExternalId(
                projectSyncDto.getOrganizationId(), projectSyncDto.getProjectName());

        ProjectSyncOutputDto configSaved;

        if (existingEntity != null) {
            configSaved = projectSyncMapper.toApi(existingEntity);
        } else {

            ConfigProjectSyncEntity configProjectSyncEntity = new ConfigProjectSyncEntity();
            configProjectSyncEntity.setId(UUID.randomUUID());
            configProjectSyncEntity.setOrganizationId(projectSyncDto.getOrganizationId());
            configProjectSyncEntity.setProjectExternalId(jiraProject.getId());

            configProjectSyncRepository.save(configProjectSyncEntity);

            configSaved = projectSyncMapper.toApi(configProjectSyncEntity);
        }

        return configSaved;
    }

    public void synchronizeAllOrgs() {
        Iterable<OrganizationEntity> orgs = organizationRepository.findAll();

        for (OrganizationEntity org : orgs) {
            jiraSyncService.synchronizeProjectsWithJira(org.getId());
        }
    }

}
