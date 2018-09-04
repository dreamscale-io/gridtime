package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.admin.ProjectSyncInputDto;
import com.dreamscale.htmflow.api.admin.ProjectSyncOutputDto;
import com.dreamscale.htmflow.api.organization.*;
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
import java.util.ArrayList;
import java.util.List;
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
    private OrganizationService organizationService;

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

    public List<MemberRegistrationDetailsDto> configureDreamScale(AutoConfigInputDto inputConfig) {

        OrganizationInputDto orgInput = new OrganizationInputDto();
        orgInput.setOrgName("DreamScale");
        orgInput.setDomainName("dreamscale.io");
        orgInput.setJiraSiteUrl("dreamscale.atlassian.net");
        orgInput.setJiraUser("janelle@dreamscale.io");
        orgInput.setJiraApiKey(inputConfig.getJiraApiKey());

        OrganizationDto dreamScaleOrg = organizationService.createOrganization(orgInput);

        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "flow-data-plugins"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "flow-platform"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "dummy-test"));

        List<MemberRegistrationDetailsDto> registrations = new ArrayList<>();

        registrations.add(registerMember(dreamScaleOrg, "janelle@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "mike@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "kara@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "bethlrichardson@gmail.com"));

        return registrations;
    }

    private MemberRegistrationDetailsDto registerMember(OrganizationDto org, String memberEmail) {
        MembershipInputDto membership = new MembershipInputDto();
        membership.setInviteToken(org.getInviteToken());
        membership.setOrgEmail(memberEmail);

        return organizationService.registerMember(org.getId(), membership);
    }


    public List<MemberRegistrationDetailsDto> configureOnPrem(AutoConfigInputDto inputConfig) {

        OrganizationInputDto orgInput = new OrganizationInputDto();
        orgInput.setOrgName("OnPrem");
        orgInput.setDomainName("onprem.com");
        orgInput.setJiraSiteUrl("onprem.atlassian.net");
        orgInput.setJiraUser("janelle_klein@onprem.com");
        orgInput.setJiraApiKey(inputConfig.getJiraApiKey());

        OrganizationDto onpremOrg = organizationService.createOrganization(orgInput);

        configureJiraProjectSync(new ProjectSyncInputDto(onpremOrg.getId(), "Toyota"));

        List<MemberRegistrationDetailsDto> registrations = new ArrayList<>();

        registrations.add(registerMember(onpremOrg, "janelle_klein@onprem.com"));
        registrations.add(registerMember(onpremOrg, "adrian@onprem.com"));
        registrations.add(registerMember(onpremOrg, "pat@onprem.com"));
        registrations.add(registerMember(onpremOrg, "steve@onprem.com"));
        registrations.add(registerMember(onpremOrg, "ckenley@onprem.com"));
        registrations.add(registerMember(onpremOrg, "costa@onprem.com"));
        registrations.add(registerMember(onpremOrg, "george@onprem.com"));
        registrations.add(registerMember(onpremOrg, "joegarcia@onprem.com"));
        registrations.add(registerMember(onpremOrg, "joshdeford@onprem.com"));

        return registrations;
    }

}
