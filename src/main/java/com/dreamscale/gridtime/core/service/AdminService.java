package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.admin.ProjectSyncInputDto;
import com.dreamscale.gridtime.api.admin.ProjectSyncOutputDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.integration.JiraCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationMembershipCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamMembershipCapability;
import com.dreamscale.gridtime.core.domain.journal.ConfigProjectSyncEntity;
import com.dreamscale.gridtime.core.domain.journal.ConfigProjectSyncRepository;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigEntity;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;
import com.dreamscale.gridtime.core.hooks.jira.dto.JiraProjectDto;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.CPGBucketConfig;
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
    private JiraCapability jiraCapability;

    @Autowired
    private JiraSyncService jiraSyncService;

    @Autowired
    private ConfigProjectSyncRepository configProjectSyncRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationMembershipCapability organizationMembership;

    @Autowired
    private TeamMembershipCapability teamMembership;

    @Autowired
    GridBoxBucketConfigRepository gridBoxBucketConfigRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectSyncOutputDto, ConfigProjectSyncEntity> projectSyncMapper;

    @PostConstruct
    private void init() {
        projectSyncMapper = mapperFactory.createDtoEntityMapper(ProjectSyncOutputDto.class, ConfigProjectSyncEntity.class);
    }

    public void configureOnPremBuckets() {
        String domainName = "onprem.com";
        String teamName = "CPG";
        String projectName = "NBCU CPG";

        OrganizationDto organizationDto = organizationMembership.getOrganizationByDomainName(domainName);

        UUID orgId = organizationDto.getId();

        TeamDto teamDto = teamMembership.getTeamByName(orgId, teamName);

        ProjectEntity projectEntity = projectRepository.findByOrganizationIdAndName(orgId, projectName);

        List<BoxMatcherConfig> boxMatcherConfigs = new CPGBucketConfig().createBoxMatchers();

        saveTeamProjectBuckets(teamDto.getId(), projectEntity.getId(), boxMatcherConfigs);

    }

    public void saveTeamProjectBuckets(UUID teamId, UUID projectId, List<BoxMatcherConfig> boxMatcherConfigs) {

        for (BoxMatcherConfig config : boxMatcherConfigs) {
            GridBoxBucketConfigEntity configEntity = new GridBoxBucketConfigEntity();
            configEntity.setId(UUID.randomUUID());
            configEntity.setTeamId(teamId);
            configEntity.setProjectId(projectId);
            configEntity.setBoxName(config.getBox());
            configEntity.setBoxMatcherConfigJson(JSONTransformer.toJson(config));

            gridBoxBucketConfigRepository.save(configEntity);
        }

    }

    public ProjectSyncOutputDto configureJiraProjectSync(ProjectSyncInputDto projectSyncDto) {

        OrganizationEntity organizationEntity = organizationRepository.findById(projectSyncDto.getOrganizationId());
        if (organizationEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        JiraProjectDto jiraProject = jiraCapability.getProjectByName(organizationEntity.getId(), projectSyncDto.getProjectName());
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

        OrganizationDto dreamScaleOrg = organizationMembership.createOrganization(orgInput);

        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "flow-data-plugins"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "flow-platform"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleOrg.getId(), "dummy-test"));

        List<MemberRegistrationDetailsDto> registrations = new ArrayList<>();

        registrations.add(registerMember(dreamScaleOrg, "janelle@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "mike@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "kara@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "evan@dreamscale.io"));
        registrations.add(registerMember(dreamScaleOrg, "bethlrichardson@gmail.com"));
        registrations.add(registerMember(dreamScaleOrg, "tobias@davistobias.com"));

        TeamDto team = teamMembership.createTeam(dreamScaleOrg.getId(), registrations.get(0).getMemberId(), "Phoenix");

        List<UUID> memberIds = extractMemberIds(registrations);
        teamMembership.addMembersToTeam(dreamScaleOrg.getId(), team.getId(), memberIds);

        return registrations;
    }


    public List<MemberRegistrationDetailsDto> configureOnPrem(AutoConfigInputDto inputConfig) {

        OrganizationInputDto orgInput = new OrganizationInputDto();
        orgInput.setOrgName("OnPrem");
        orgInput.setDomainName("onprem.com");
        orgInput.setJiraSiteUrl("onprem.atlassian.net");
        orgInput.setJiraUser("janelle_klein@onprem.com");
        orgInput.setJiraApiKey(inputConfig.getJiraApiKey());

        OrganizationDto onpremOrg = organizationMembership.createOrganization(orgInput);

        configureJiraProjectSync(new ProjectSyncInputDto(onpremOrg.getId(), "Toyota"));
        configureJiraProjectSync(new ProjectSyncInputDto(onpremOrg.getId(), "NBCU CPG"));



        //toyota team
        List<MemberRegistrationDetailsDto> toyotaRegistrations = new ArrayList<>();

        toyotaRegistrations.add(registerMember(onpremOrg, "adrian@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "pat@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "steve@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "ckenley@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "costa@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "george@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "joegarcia@onprem.com"));
        toyotaRegistrations.add(registerMember(onpremOrg, "joshdeford@onprem.com"));

        List<UUID> toyotaMemberIds = extractMemberIds(toyotaRegistrations);

        TeamDto toyotaTeam = teamMembership.createTeam(onpremOrg.getId(), toyotaRegistrations.get(0).getMemberId(), "Toyota");
        teamMembership.addMembersToTeam(onpremOrg.getId(), toyotaTeam.getId(), toyotaMemberIds);

        //cpg team

        List<MemberRegistrationDetailsDto> cpgRegistrations = new ArrayList<>();

        cpgRegistrations.add(registerMember(onpremOrg, "janelle_klein@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "ashley@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "corey@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "mfitzpatrick@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "kristian@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "joseph@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "mattk@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "noel@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "richard@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "swetha@onprem.com"));

        cpgRegistrations.add(registerMember(onpremOrg, "christophe@onprem.com"));
        cpgRegistrations.add(registerMember(onpremOrg, "jeremy@onprem.com"));

        List<UUID> cpgMemberIds = extractMemberIds(cpgRegistrations);

        TeamDto cpgTeam = teamMembership.createTeam(onpremOrg.getId(), cpgRegistrations.get(0).getMemberId(), "CPG");
        teamMembership.addMembersToTeam(onpremOrg.getId(), cpgTeam.getId(), cpgMemberIds);

        List<MemberRegistrationDetailsDto> allRegistrations = new ArrayList<>();
        allRegistrations.addAll(toyotaRegistrations);
        allRegistrations.addAll(cpgRegistrations);

        return allRegistrations;
    }

    private List<UUID> extractMemberIds(List<MemberRegistrationDetailsDto> registrations) {
        List<UUID> memberIds = new ArrayList<>();
        for (MemberRegistrationDetailsDto member: registrations) {
            memberIds.add(member.getMemberId());
        }
        return memberIds;
    }

    private MemberRegistrationDetailsDto registerMember(OrganizationDto org, String memberEmail) {
        MembershipInputDto membership = new MembershipInputDto();
        membership.setInviteToken(org.getInviteToken());
        membership.setOrgEmail(memberEmail);

        return organizationMembership.registerMember(org.getId(), membership);
    }


}
