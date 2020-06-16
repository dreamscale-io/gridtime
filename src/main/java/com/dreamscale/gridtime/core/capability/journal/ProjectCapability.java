package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.CreateProjectInputDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.ProjectEntity;
import com.dreamscale.gridtime.core.domain.journal.ProjectRepository;
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
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;

    private static final String DEFAULT_PROJECT_NAME = "No Project";
    private static final String DEFAULT_PROJECT_DESCRIPTION = "(No Project Selected)";

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
    }

    @Transactional
    public ProjectDto findOrCreateProject(LocalDateTime now, UUID organizationId, CreateProjectInputDto projectInputDto) {

        String standardizedProjectName = standardizeToLowerCase(projectInputDto.getName());

        ProjectEntity orgProject = projectRepository.findByOrganizationIdAndLowercaseName(organizationId, standardizedProjectName);

        if (orgProject == null) {
            orgProject = new ProjectEntity();
            orgProject.setId(UUID.randomUUID());
            orgProject.setName(projectInputDto.getName());
            orgProject.setDescription(projectInputDto.getDescription());
            orgProject.setOrganizationId(organizationId);
            orgProject.setPrivate(true);

            projectRepository.save(orgProject);

        }

        return projectMapper.toApi(orgProject);
    }

    @Transactional
    public ProjectDto createDefaultProject(LocalDateTime now, UUID organizationId) {

        ProjectEntity orgProject = new ProjectEntity();
        orgProject.setId(UUID.randomUUID());
        orgProject.setName(DEFAULT_PROJECT_NAME);
        orgProject.setDescription(DEFAULT_PROJECT_DESCRIPTION);
        orgProject.setLowercaseName(DEFAULT_PROJECT_NAME.toLowerCase());
        orgProject.setOrganizationId(organizationId);

        projectRepository.save(orgProject);

        return projectMapper.toApi(orgProject);
    }

    public ProjectDto findDefaultProject(UUID organizationId) {

        ProjectEntity project = projectRepository.findByOrganizationIdAndLowercaseName(organizationId, DEFAULT_PROJECT_NAME.toLowerCase());

        return projectMapper.toApi(project);
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