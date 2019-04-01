package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
    }

    public List<ProjectDto> getAllProjects(UUID organizationId) {

        Iterable<ProjectEntity> projectEntities = projectRepository.findByOrganizationId(organizationId);
        return projectMapper.toApiList(projectEntities);
    }


}