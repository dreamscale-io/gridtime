package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.account.RootAccountCredentialsInputDto;
import com.dreamscale.gridtime.api.account.UserProfileDto;
import com.dreamscale.gridtime.api.admin.ProjectSyncInputDto;
import com.dreamscale.gridtime.api.admin.ProjectSyncOutputDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.external.JiraSyncCapability;
import com.dreamscale.gridtime.core.capability.membership.RootAccountCapability;
import com.dreamscale.gridtime.core.capability.external.JiraCapability;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.ConfigProjectSyncEntity;
import com.dreamscale.gridtime.core.domain.journal.ConfigProjectSyncRepository;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
import com.dreamscale.gridtime.core.domain.member.OrganizationEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationRepository;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigEntity;
import com.dreamscale.gridtime.core.domain.tile.GridBoxBucketConfigRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class AdminService {

    @Autowired
    private RootAccountCapability accountCapability;

    @Autowired
    private JiraCapability jiraCapability;

    @Autowired
    private JiraSyncCapability jiraSyncCapability;

    @Autowired
    private ConfigProjectSyncRepository configProjectSyncRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private TeamCapability teamCapability;

    @Autowired
    private GridClock gridClock;

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

        OrganizationDto organizationDto = organizationCapability.getOrganizationByDomainName(domainName);

        UUID orgId = organizationDto.getId();

        TeamDto teamDto = teamCapability.getTeamByName(orgId, teamName);

        ProjectEntity projectEntity = projectRepository.findPublicProjectByName(orgId, projectName);

        List<BoxMatcherConfig> boxMatcherConfigs = new CPGBucketConfig().createBoxMatchers();

        saveTeamProjectBuckets(teamDto.getId(), projectEntity.getId(), boxMatcherConfigs);

    }

    public void saveTeamProjectBuckets(UUID teamId, UUID projectId, List<BoxMatcherConfig> boxMatcherConfigs) {

        for (BoxMatcherConfig config : boxMatcherConfigs) {
            GridBoxBucketConfigEntity configEntity = new GridBoxBucketConfigEntity();
            configEntity.setId(UUID.randomUUID());
            configEntity.setOrganizationId(teamId);
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
            jiraSyncCapability.synchronizeProjectsWithJira(org.getId());
        }
    }

    public List<MemberRegistrationDetailsDto> configureDreamScale(AutoConfigInputDto inputConfig) {

        UserProfileDto account1 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("janelle@dreamscale.io", "password"));
        UserProfileDto account2 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("mike@dreamscale.io", "password"));
        UserProfileDto account3 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("kara@dreamscale.io", "password"));
        UserProfileDto account4 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("zoe@dreamscale.io", "password"));
        UserProfileDto account5 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("evan@dreamscale.io", "password"));

        SubscriptionInputDto orgInput = new SubscriptionInputDto();
        orgInput.setOrganizationName("DreamScale");
        orgInput.setDomainName("dreamscale.io");
        orgInput.setStripePaymentId("[STRIPE_PAYMENT]");
        orgInput.setRequireMemberEmailInDomain(true);
        orgInput.setSeats(10);

        OrganizationSubscriptionDto dreamScaleSubscription = organizationCapability.createOrganizationSubscription(account1.getRootAccountId(), orgInput);

        MemberRegistrationDetailsDto member1 = joinOrganization(account1, dreamScaleSubscription);
        MemberRegistrationDetailsDto member2 = joinOrganization(account2, dreamScaleSubscription);
        MemberRegistrationDetailsDto member3 = joinOrganization(account3, dreamScaleSubscription);
        MemberRegistrationDetailsDto member4 = joinOrganization(account4, dreamScaleSubscription);
        MemberRegistrationDetailsDto member5 = joinOrganization(account5, dreamScaleSubscription);

        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleSubscription.getOrganizationId(), "flow-data-plugins"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleSubscription.getOrganizationId(), "flow-platform"));
        configureJiraProjectSync(new ProjectSyncInputDto(dreamScaleSubscription.getOrganizationId(), "dummy-test"));

        TeamDto team = teamCapability.createTeam(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix");

        teamCapability.addMemberToTeamWithMemberId(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix", member1.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix", member2.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix", member3.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix", member4.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(dreamScaleSubscription.getOrganizationId(), member1.getMemberId(), "Phoenix", member5.getMemberId());

        return DefaultCollections.toList(member1, member2, member3, member4, member5);
    }

    private MemberRegistrationDetailsDto joinOrganization(UserProfileDto account, OrganizationSubscriptionDto subscription) {
        LocalDateTime now = gridClock.now();

        return organizationCapability.forceJoinOrganization(now, account.getRootAccountId(), subscription.getOrganizationId(), account.getRootEmail());
    }


    public List<MemberRegistrationDetailsDto> configureOnPrem(AutoConfigInputDto inputConfig) {

        UserProfileDto account1 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("adrian@onprem.com", "password"));
        UserProfileDto account2 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("pat@onprem.com", "password"));
        UserProfileDto account3 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("steve@onprem.com", "password"));
        UserProfileDto account4 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("ckenley@onprem.com", "password"));
        UserProfileDto account5 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("costa@onprem.com", "password"));
        UserProfileDto account6 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("george@onprem.com", "password"));
        UserProfileDto account7 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("joegarcia@onprem.com", "password"));
        UserProfileDto account8 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("joshdeford@onprem.com", "password"));

        SubscriptionInputDto orgInput = new SubscriptionInputDto();
        orgInput.setOrganizationName("OnPrem");
        orgInput.setDomainName("onprem.com");
        orgInput.setStripePaymentId("[STRIPE_PAYMENT]");
        orgInput.setRequireMemberEmailInDomain(true);
        orgInput.setSeats(10);

        OrganizationSubscriptionDto onPremSubscription = organizationCapability.createOrganizationSubscription(account1.getRootAccountId(), orgInput);

        MemberRegistrationDetailsDto member1 = joinOrganization(account1, onPremSubscription);
        MemberRegistrationDetailsDto member2 = joinOrganization(account2, onPremSubscription);
        MemberRegistrationDetailsDto member3 = joinOrganization(account3, onPremSubscription);
        MemberRegistrationDetailsDto member4 = joinOrganization(account4, onPremSubscription);
        MemberRegistrationDetailsDto member5 = joinOrganization(account5, onPremSubscription);
        MemberRegistrationDetailsDto member6 = joinOrganization(account6, onPremSubscription);
        MemberRegistrationDetailsDto member7 = joinOrganization(account7, onPremSubscription);
        MemberRegistrationDetailsDto member8 = joinOrganization(account8, onPremSubscription);


        configureJiraProjectSync(new ProjectSyncInputDto(onPremSubscription.getOrganizationId(), "Toyota"));
        configureJiraProjectSync(new ProjectSyncInputDto(onPremSubscription.getOrganizationId(), "NBCU CPG"));

        TeamDto toyotaTeam = teamCapability.createTeam(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota");

        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member1.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member2.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member3.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member4.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member5.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member6.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member7.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), member1.getMemberId(), "Toyota", member8.getMemberId());

        //cpg team

        UserProfileDto cpg1 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("janelle_klein@onprem.com", "password"));
        UserProfileDto cpg2 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("ashley@onprem.com", "password"));
        UserProfileDto cpg3 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("corey@onprem.com", "password"));
        UserProfileDto cpg4 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("mfitzpatrick@onprem.com", "password"));
        UserProfileDto cpg5 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("kristian@onprem.com", "password"));
        UserProfileDto cpg6 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("joseph@onprem.com", "password"));
        UserProfileDto cpg7 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("mattk@onprem.com", "password"));
        UserProfileDto cpg8 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("noel@onprem.com", "password"));
        UserProfileDto cpg9 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("richard@onprem.com", "password"));
        UserProfileDto cpg10 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("swetha@onprem.com", "password"));
        UserProfileDto cpg11 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("christophe@onprem.com", "password"));
        UserProfileDto cpg12 = accountCapability.registerAccount(new RootAccountCredentialsInputDto("jeremy@onprem.com", "password"));

        MemberRegistrationDetailsDto cpgmember1 = joinOrganization(cpg1, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember2 = joinOrganization(cpg2, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember3 = joinOrganization(cpg3, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember4 = joinOrganization(cpg4, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember5 = joinOrganization(cpg5, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember6 = joinOrganization(cpg6, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember7 = joinOrganization(cpg7, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember8 = joinOrganization(cpg8, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember9 = joinOrganization(cpg9, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember10 = joinOrganization(cpg10, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember11 = joinOrganization(cpg11, onPremSubscription);
        MemberRegistrationDetailsDto cpgmember12 = joinOrganization(cpg12, onPremSubscription);

        TeamDto cpgTeam = teamCapability.createTeam(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG");

        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember1.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember2.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember3.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember4.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember5.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember6.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember7.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember8.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember9.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember10.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember11.getMemberId());
        teamCapability.addMemberToTeamWithMemberId(onPremSubscription.getOrganizationId(), cpgmember1.getMemberId(), "CPG", cpgmember12.getMemberId());


        return DefaultCollections.toList(member1, member2, member3, member4, member5, member6, member7, member8,
                cpgmember1, cpgmember2, cpgmember3, cpgmember4, cpgmember5, cpgmember6, cpgmember7, cpgmember8, cpgmember9, cpgmember10, cpgmember11, cpgmember12);
    }



}
