package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.journal.ProjectDto;
import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.core.context.domain.ProjectEntity;
import com.dreamscale.htmflow.core.context.domain.ProjectRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {

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
