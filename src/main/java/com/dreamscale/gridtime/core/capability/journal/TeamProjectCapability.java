package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.CreateProjectInputDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.TeamProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.TeamProjectRepository;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class TeamProjectCapability {

    @Autowired
    private GridClock gridClock;

    @Autowired
    TeamProjectRepository teamProjectRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<ProjectDto, TeamProjectEntity> teamProjectMapper;

    private static final String DEFAULT_PROJECT_NAME = "No Project";

    @PostConstruct
    private void init() {
        teamProjectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, TeamProjectEntity.class);
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
    }

    @Transactional
    public ProjectDto findOrCreateTeamProject(LocalDateTime now, UUID organizationId, UUID invokingMemberId, UUID teamId, CreateProjectInputDto projectInputDto) {

        String standardizedProjectName = standardizeToLowerCase(projectInputDto.getName());

        TeamProjectEntity teamProject = teamProjectRepository.findByTeamIdAndLowercaseName(teamId, standardizedProjectName);

        if (teamProject == null) {
            teamProject = new TeamProjectEntity();
            teamProject.setId(UUID.randomUUID());
            teamProject.setOrganizationId(organizationId);
            teamProject.setTeamId(teamId);
            teamProject.setCreatorId(invokingMemberId);
            teamProject.setName(projectInputDto.getName());
            teamProject.setDescription(projectInputDto.getDescription());
            teamProject.setLowercaseName(standardizedProjectName);
            teamProject.setCreationDate(now);

            teamProjectRepository.save(teamProject);

            ProjectEntity orgProject = new ProjectEntity();
            orgProject.setId(teamProject.getId());
            orgProject.setName(teamProject.getName());
            orgProject.setOrganizationId(organizationId);

            projectRepository.save(orgProject);

        }

        return toDto(teamProject);
    }

    @Transactional
    public ProjectDto createDefaultTeamProject(LocalDateTime now, UUID organizationId, UUID teamId, UUID creatorId) {

        TeamProjectEntity defaultProject = new TeamProjectEntity();
        defaultProject.setId(UUID.randomUUID());
        defaultProject.setOrganizationId(organizationId);
        defaultProject.setTeamId(teamId);
        defaultProject.setCreatorId(creatorId);
        defaultProject.setName(DEFAULT_PROJECT_NAME);
        defaultProject.setLowercaseName(DEFAULT_PROJECT_NAME.toLowerCase());
        defaultProject.setCreationDate(now);

        teamProjectRepository.save(defaultProject);

        ProjectEntity orgProject = new ProjectEntity();
        orgProject.setId(defaultProject.getId());
        orgProject.setName(defaultProject.getName());
        orgProject.setOrganizationId(organizationId);

        projectRepository.save(orgProject);


        return toDto(defaultProject);
    }

    public ProjectDto findDefaultProjectForTeam(UUID organizationId, UUID teamId) {

        TeamProjectEntity defaultProject = teamProjectRepository.findByTeamIdAndLowercaseName(teamId, DEFAULT_PROJECT_NAME.toLowerCase());
        return teamProjectMapper.toApi(defaultProject);
    }


    public List<ProjectDto> getAllTeamProjects(UUID organizationId, UUID teamId) {

        List<TeamProjectEntity> teamProjects = teamProjectRepository.findByTeamId(teamId);

        return teamProjectMapper.toApiList(teamProjects);
    }

    private ProjectDto toDto(TeamProjectEntity teamProject) {
        ProjectDto projectDto = new ProjectDto();

        projectDto.setId(teamProject.getId());
        projectDto.setName(teamProject.getName());

        return projectDto;
    }

    public List<ProjectDto> getAllProjects(UUID organizationId) {
        Iterable<ProjectEntity> projectEntities = projectRepository.findByOrganizationId(organizationId);
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
        //TODO once we deprecate the old APIs, we should migrate the old data over, and make this work from team_project tables

        List<ProjectEntity> projectEntities = projectRepository.findByRecentMemberAccess(memberId);
        return projectMapper.toApiList(projectEntities);
    }

    public List<ProjectDto> findProjectsByRecentTeamMemberAccess(UUID organizationId, UUID teamId) {

        List<ProjectEntity> projectEntities = projectRepository.findByRecentTeamMemberAccess(organizationId, teamId);
        return projectMapper.toApiList(projectEntities);

    }


}