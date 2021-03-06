package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.journal.GrantType;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberRepository;
import com.dreamscale.gridtime.core.domain.member.TeamEntity;
import com.dreamscale.gridtime.core.domain.member.TeamRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProjectCapability {

    @Autowired
    private GridClock gridClock;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskCapability taskCapability;

    @Autowired
    private ProjectGrantAccessRepository projectGrantAccessRepository;

    @Autowired
    private ProjectGrantTombstoneRepository projectGrantTombstoneRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;

    private DtoEntityMapper<ProjectDetailsDto, ProjectEntity> projectDetailsMapper;

    @PostConstruct
    private void init() {

        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        projectDetailsMapper = mapperFactory.createDtoEntityMapper(ProjectDetailsDto.class, ProjectEntity.class);
    }

    @Transactional
    public ProjectDto findOrCreateProject(LocalDateTime now, UUID organizationId, UUID memberId, CreateProjectInputDto projectInputDto) {

        String standardizedProjectName = standardizeToLowerCase(projectInputDto.getName());

        ProjectEntity project = findExistingProjectByNameWithPermission(organizationId, memberId, standardizedProjectName);

        if (project == null) {
            project = new ProjectEntity();
            project.setId(UUID.randomUUID());
            project.setName(projectInputDto.getName());
            project.setLowercaseName(standardizedProjectName);
            project.setDescription(projectInputDto.getDescription());
            project.setOrganizationId(organizationId);
            project.setCreatedBy(memberId);
            project.setCreatedDate(now);
            project.setPrivate(projectInputDto.isPrivate());

            if (projectInputDto.isPrivate()) {
                createGrantAccessForMember(now, organizationId, memberId, memberId, project);
            }

            projectRepository.save(project);

            taskCapability.createDefaultProjectTask(organizationId, project.getId(), project.isPrivate());
        }

        return projectMapper.toApi(project);
    }

    private ProjectEntity findExistingProjectByNameWithPermission(UUID organizationId, UUID memberId, String standardizedProjectName) {
        ProjectEntity project = null;

        ProjectEntity privateProject = findPrivateProject(organizationId, memberId, standardizedProjectName);
        ProjectEntity publicProject = projectRepository.findPublicProjectByName(organizationId, standardizedProjectName);


        if (privateProject != null) {
            project = privateProject;
            log.debug("Found private project "+standardizedProjectName);
        }

        if (project == null && publicProject != null) {
            project = publicProject;
            log.debug("Found public project "+standardizedProjectName);
        }
        return project;
    }


    public ProjectDto getProjectByName(UUID organizationId, UUID memberId, String projectName) {

        String standardizedProjectName = standardizeToLowerCase(projectName);

        ProjectEntity project = findExistingProjectByNameWithPermission(organizationId, memberId, standardizedProjectName);

        return projectMapper.toApi(project);
    }

    public ProjectDto getProjectById(UUID organizationId, UUID memberId, UUID projectId) {
        ProjectEntity project = findExistingProjectByIdWithPermission(organizationId, memberId, projectId);

        return projectMapper.toApi(project);
    }

    private ProjectEntity findPrivateProject(UUID organizationId, UUID memberId, String standardizedProjectName) {
        List<ProjectEntity> projects = projectRepository.findPrivateProjectByName(organizationId, memberId, standardizedProjectName);

        ProjectEntity project = null;

        if (projects.size() > 0) {
            project = projects.get(0);

            for (ProjectEntity projectRef : projects) {
                if (projectRef.getCreatedBy().equals(memberId)) {
                    project = projectRef;
                }
            }
        }

        return project;
    }

    public void validateProjectPermission(UUID organizationId, UUID memberId, UUID projectId) {

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        if (project != null && project.isPrivate()) {
            project = projectRepository.findPrivateProjectById(organizationId, memberId, projectId);
        }

        validateProjectHasPermission(project);
    }

    private void createGrantAccessForMember(LocalDateTime now, UUID organizationId, UUID fromMemberId, UUID toMemberId, ProjectEntity orgProject) {
        ProjectGrantAccessEntity projectGrantAccessEntity = new ProjectGrantAccessEntity();

        projectGrantAccessEntity.setId(UUID.randomUUID());
        projectGrantAccessEntity.setOrganizationId(organizationId);
        projectGrantAccessEntity.setProjectId(orgProject.getId());
        projectGrantAccessEntity.setGrantedDate(now);
        projectGrantAccessEntity.setGrantedById(fromMemberId);
        projectGrantAccessEntity.setGrantType(GrantType.MEMBER);
        projectGrantAccessEntity.setGrantedToId(toMemberId);

        projectGrantAccessRepository.save(projectGrantAccessEntity);
    }

    private void createGrantAccessForTeam(LocalDateTime now, UUID organizationId, UUID fromMemberId, UUID teamId, ProjectEntity project) {
        ProjectGrantAccessEntity projectGrantAccessEntity = new ProjectGrantAccessEntity();

        projectGrantAccessEntity.setId(UUID.randomUUID());
        projectGrantAccessEntity.setOrganizationId(organizationId);
        projectGrantAccessEntity.setProjectId(project.getId());
        projectGrantAccessEntity.setGrantedDate(now);
        projectGrantAccessEntity.setGrantedById(fromMemberId);
        projectGrantAccessEntity.setGrantType(GrantType.TEAM);
        projectGrantAccessEntity.setGrantedToId(teamId);

        projectGrantAccessRepository.save(projectGrantAccessEntity);
    }


    @Transactional
    public SimpleStatusDto grantAccessForMember(UUID organizationId, UUID invokingMemberId, UUID projectId, String username) {

        LocalDateTime now = gridClock.now();

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);
        validateProjectIsPrivate(project);

        validateProjectIsOwnedByMember(project, invokingMemberId);

        OrganizationMemberEntity memberToGrant = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, standardizeToLowerCase(username));

        validateMemberFound(username, memberToGrant);

        ProjectGrantAccessEntity grant = projectGrantAccessRepository.findByProjectIdAndGrantTypeAndGrantedToId(projectId, GrantType.MEMBER, memberToGrant.getId());

        if (grant == null) {
            createGrantAccessForMember(now, organizationId, invokingMemberId, memberToGrant.getId(), project);
        }

        return new SimpleStatusDto(Status.SUCCESS, "Granted access to project "+ project.getName() + " for "+username);
    }

    public SimpleStatusDto revokeAccessForMember(UUID organizationId, UUID invokingMemberId, UUID projectId, String username) {
        LocalDateTime now = gridClock.now();

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);
        validateProjectIsPrivate(project);
        validateProjectIsOwnedByMember(project, invokingMemberId);

        OrganizationMemberEntity memberToRevoke = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, standardizeToLowerCase(username));

        validateMemberFound(username, memberToRevoke);

        ProjectGrantAccessEntity grant = projectGrantAccessRepository.findByProjectIdAndGrantTypeAndGrantedToId(projectId, GrantType.MEMBER, memberToRevoke.getId());

        validateExistingGrantFound(grant);

        convertToTombstone(now, project, grant);

        return new SimpleStatusDto(Status.SUCCESS, "Revoked access to project "+ project.getName() + " for "+username);

    }

    public SimpleStatusDto grantAccessForTeam(UUID organizationId, UUID invokingMemberId, UUID projectId, String teamName) {

        LocalDateTime now = gridClock.now();

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);
        validateProjectIsPrivate(project);

        validateProjectIsOwnedByMember(project, invokingMemberId);

        TeamEntity teamToGrant = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizeToLowerCase(teamName));

        validateTeamFound(teamName, teamToGrant);

        ProjectGrantAccessEntity grant = projectGrantAccessRepository.findByProjectIdAndGrantTypeAndGrantedToId(projectId, GrantType.TEAM, teamToGrant.getId());

        if (grant == null) {
            createGrantAccessForTeam(now, organizationId, invokingMemberId, teamToGrant.getId(), project);
        }

        return new SimpleStatusDto(Status.SUCCESS, "Granted access to project "+ project.getName() + " for team "+teamName);
    }

    public SimpleStatusDto revokeAccessForTeam(UUID organizationId, UUID invokingMemberId, UUID projectId, String teamName) {

        LocalDateTime now = gridClock.now();

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);
        validateProjectIsPrivate(project);
        validateProjectIsOwnedByMember(project, invokingMemberId);

        TeamEntity teamToRevoke = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizeToLowerCase(teamName));

        validateTeamFound(teamName, teamToRevoke);

        ProjectGrantAccessEntity grant = projectGrantAccessRepository.findByProjectIdAndGrantTypeAndGrantedToId(projectId, GrantType.TEAM, teamToRevoke.getId());

        validateExistingGrantFound(grant);

        convertToTombstone(now, project, grant);

        return new SimpleStatusDto(Status.SUCCESS, "Revoked access to project "+ project.getName() + " for team "+teamName);
    }

    private void convertToTombstone(LocalDateTime now, ProjectEntity project, ProjectGrantAccessEntity grant) {

        ProjectGrantTombstoneEntity tombstoneEntity = new ProjectGrantTombstoneEntity();
        tombstoneEntity.setId(UUID.randomUUID());
        tombstoneEntity.setGrantId(grant.getId());
        tombstoneEntity.setOrganizationId(grant.getOrganizationId());
        tombstoneEntity.setProjectId(grant.getProjectId());
        tombstoneEntity.setGrantType(grant.getGrantType());
        tombstoneEntity.setGrantedById(grant.getGrantedById());
        tombstoneEntity.setGrantedToId(grant.getGrantedToId());
        tombstoneEntity.setGrantedDate(grant.getGrantedDate());
        tombstoneEntity.setProjectName(project.getName());
        tombstoneEntity.setRipDate(now);

        projectGrantTombstoneRepository.save(tombstoneEntity);

        projectGrantAccessRepository.delete(grant);
    }

    private void validateMemberFound(String username, OrganizationMemberEntity memberToGrant) {
        if (memberToGrant == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_FOUND, "Member "+username + " not found.");
        }
    }

    private void validateTeamFound(String teamName, TeamEntity teamToGrant) {
        if (teamToGrant == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Team "+teamName + " not found.");
        }
    }

    private void validateExistingGrantFound(ProjectGrantAccessEntity grant) {
        if (grant == null) {
            throw new BadRequestException(ValidationErrorCodes.EXISTING_GRANT_NOT_FOUND, "Unable to find the grant to revoke.");
        }
    }


    private void validateProjectIsOwnedByMember(ProjectEntity project, UUID invokingMemberId) {
        if (!invokingMemberId.equals(project.getCreatedBy())) {
            throw new BadRequestException(ValidationErrorCodes.PERMISSION_NOT_AUTHORIZED, "Unable to grant access to a project not owned by user.");
        }
    }

    private void validateProjectIsPrivate(ProjectEntity project) {
        if (!project.isPrivate()) {
            throw new BadRequestException(ValidationErrorCodes.PROJECT_MUST_BE_PRIVATE_TO_GRANT_ACCESS, "Project must be private to grant access.");
        }
    }

    private void validateProjectFound(String reference, ProjectEntity project) {
        if (project == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Project {} not found", reference);
        }
    }

    private void validateProjectHasPermission(ProjectEntity project) {
        if (project == null) {
            throw new BadRequestException(ValidationErrorCodes.PERMISSION_NOT_AUTHORIZED, "No permission to access project.");
        }
    }

    @Transactional
    public ProjectDto createDefaultProject(LocalDateTime now, UUID organizationId) {

        ProjectEntity orgProject = new ProjectEntity();
        orgProject.setId(UUID.randomUUID());
        orgProject.configureDefault();
        orgProject.setOrganizationId(organizationId);
        orgProject.setCreatedDate(now);
        orgProject.setPrivate(false);

        projectRepository.save(orgProject);

        taskCapability.createDefaultProjectTask(organizationId, orgProject.getId(), orgProject.isPrivate());

        return projectMapper.toApi(orgProject);
    }

    public ProjectDto findDefaultProject(UUID organizationId) {

        ProjectEntity project = projectRepository.findPublicProjectByName(organizationId, ProjectEntity.DEFAULT_PROJECT_NAME.toLowerCase());

        return projectMapper.toApi(project);
    }


    public ProjectDetailsDto getProjectDetails(UUID organizationId, UUID invokingMemberId, UUID projectId) {
        ProjectEntity project = findExistingProjectByIdWithPermission(organizationId, invokingMemberId, projectId);

        ProjectDetailsDto projectDetailsDto = projectDetailsMapper.toApi(project);

        List<ProjectGrantAccessEntity> accessGrants = projectGrantAccessRepository.findByProjectId(project.getId());

        for (ProjectGrantAccessEntity grant: accessGrants) {
            AccessGrantDto grantDto = new AccessGrantDto();
            grantDto.setGrantType(grant.getGrantType().name());
            grantDto.setGrantedToId(grant.getGrantedToId());
            grantDto.setGrantedToName(lookupGrantToName(grant));

            projectDetailsDto.addAccessGrant(grantDto);
        }

        return projectDetailsDto;
    }

    private String lookupGrantToName(ProjectGrantAccessEntity grant) {
        if (grant.getGrantType().equals(GrantType.MEMBER)) {
            return memberDetailsRetriever.lookupUsername(grant.getGrantedToId());
        } else if (grant.getGrantType().equals(GrantType.TEAM)) {
            TeamEntity team = teamRepository.findById(grant.getGrantedToId());
            return team.getName();
        }

        return null;
    }

    private ProjectEntity findExistingProjectByIdWithPermission(UUID organizationId, UUID memberId, UUID projectId) {

        ProjectEntity project = projectRepository.findByOrganizationIdAndId(organizationId, projectId);

        validateProjectFound(projectId.toString(), project);

        if (project.isPrivate()) {
            project = projectRepository.findPrivateProjectById(organizationId, memberId, projectId);
        }

        validateProjectHasPermission(project);

        return project;
    }

    public List<ProjectDto> getAllProjectsWithPermission(UUID organizationId, UUID memberId) {
        Iterable<ProjectEntity> projectEntities = projectRepository.findByOrganizationIdAndPermission(organizationId, memberId);
        return projectMapper.toApiList(projectEntities);
    }

    private String standardizeToLowerCase(String name) {
        if (name != null) {
            return name.toLowerCase();
        }
        return null;
    }

    public UUID getOrganizationIdForTeamProject(UUID projectId) {

        ProjectEntity projectEntity = projectRepository.findOne(projectId);

        if (projectEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Team project not found");
        } else {
            return projectEntity.getOrganizationId();
        }
    }

    public List<ProjectDto> findProjectsByRecentMemberAccess(UUID organizationId, UUID memberId) {

        List<ProjectEntity> projectEntities = projectRepository.findByRecentMemberAccess(organizationId, memberId);
        return projectMapper.toApiList(projectEntities);
    }

    public List<ProjectDto> findProjectsByMemberPermission(UUID organizationId, UUID memberId, int limit) {

        List<ProjectEntity> projectEntities = projectRepository.findByOrganizationIdAndPermissionWithLimit(organizationId, memberId, limit);
        return projectMapper.toApiList(projectEntities);
    }

    public SimpleStatusDto updateBoxConfiguration(UUID organizationId, UUID invokingMemberId, UUID projectId, ProjectBoxConfigurationInputDto projectBoxConfiguration) {

        return new SimpleStatusDto(Status.NO_ACTION, "Not yet implemented");
    }



}