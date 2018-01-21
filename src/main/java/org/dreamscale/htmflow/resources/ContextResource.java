package org.dreamscale.htmflow.resources;

import org.dreamscale.htmflow.api.ResourcePaths;
import org.dreamscale.htmflow.api.context.ProjectDto;
import org.dreamscale.htmflow.core.context.domain.ProjectEntity;
import org.dreamscale.htmflow.core.context.domain.ProjectRepository;
import org.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import org.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping(produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class ContextResource {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
    }

    @GetMapping(ResourcePaths.PROJECT_PATH)
    List<ProjectDto> getProjects() {
        Iterable<ProjectEntity> projectEntities = projectRepository.findAll();
        return projectMapper.toApiList(projectEntities);
    }

}
