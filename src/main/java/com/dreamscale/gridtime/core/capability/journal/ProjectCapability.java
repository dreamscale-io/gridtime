package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.project.CreateProjectInputDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.journal.*;
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
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;

    private static final String DEFAULT_PROJECT_NAME = "No Project";
    private static final String DEFAULT_PROJECT_DESCRIPTION = "(No Project Selected)";

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
    }

    @Transactional
    public ProjectDto findOrCreateProject(LocalDateTime now, UUID organizationId, UUID memberId, CreateProjectInputDto projectInputDto) {

        String standardizedProjectName = standardizeToLowerCase(projectInputDto.getName());

        ProjectEntity project = null;

        if (projectInputDto.isPrivate()) {
            project = projectRepository.findPrivateProjectByName(organizationId, memberId, standardizedProjectName);
        } else {
            project = projectRepository.findPublicProjectByName(organizationId, standardizedProjectName);
        }

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
                createGrantAccessForMember(now, organizationId, memberId, project);
            }

            projectRepository.save(project);

            taskCapability.createDefaultProjectTask(organizationId, project.getId());
        }

        return projectMapper.toApi(project);
    }

    private void createGrantAccessForMember(LocalDateTime now, UUID organizationId, UUID memberId, ProjectEntity orgProject) {
        ProjectGrantAccessEntity projectGrantAccessEntity = new ProjectGrantAccessEntity();

        projectGrantAccessEntity.setId(UUID.randomUUID());
        projectGrantAccessEntity.setOrganizationId(organizationId);
        projectGrantAccessEntity.setProjectId(orgProject.getId());
        projectGrantAccessEntity.setGrantedDate(now);
        projectGrantAccessEntity.setGrantedById(memberId);
        projectGrantAccessEntity.setGrantType(GrantType.MEMBER);
        projectGrantAccessEntity.setGrantedToId(memberId);

        projectGrantAccessRepository.save(projectGrantAccessEntity);
    }

    @Transactional
    public ProjectDto createDefaultProject(LocalDateTime now, UUID organizationId) {

        ProjectEntity orgProject = new ProjectEntity();
        orgProject.setId(UUID.randomUUID());
        orgProject.setName(DEFAULT_PROJECT_NAME);
        orgProject.setDescription(DEFAULT_PROJECT_DESCRIPTION);
        orgProject.setLowercaseName(DEFAULT_PROJECT_NAME.toLowerCase());
        orgProject.setOrganizationId(organizationId);
        orgProject.setCreatedDate(now);
        orgProject.setPrivate(false);

        projectRepository.save(orgProject);

        taskCapability.createDefaultProjectTask(organizationId, orgProject.getId());

        return projectMapper.toApi(orgProject);
    }

    public ProjectDto findDefaultProject(UUID organizationId) {

        ProjectEntity project = projectRepository.findPublicProjectByName(organizationId, DEFAULT_PROJECT_NAME.toLowerCase());

        return projectMapper.toApi(project);
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

        List<ProjectEntity> projectEntities = projectRepository.findByRecentMemberAccess(memberId);
        return projectMapper.toApiList(projectEntities);
    }

    public List<ProjectDto> findProjectsByMemberPermission(UUID organizationId, UUID memberId, int limit) {

        List<ProjectEntity> projectEntities = projectRepository.findByOrganizationIdAndPermissionWithLimit(organizationId, memberId, limit);
        return projectMapper.toApiList(projectEntities);

    }


}