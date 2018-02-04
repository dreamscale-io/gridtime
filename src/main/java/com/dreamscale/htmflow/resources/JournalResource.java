package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.ProjectEntity;
import com.dreamscale.htmflow.core.domain.ProjectRepository;
import com.dreamscale.htmflow.core.domain.TaskEntity;
import com.dreamscale.htmflow.core.domain.TaskRepository;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ProjectDto, ProjectEntity> projectMapper;
    private DtoEntityMapper<TaskDto, TaskEntity> taskMapper;

    @PostConstruct
    private void init() {
        projectMapper = mapperFactory.createDtoEntityMapper(ProjectDto.class, ProjectEntity.class);
        taskMapper = mapperFactory.createDtoEntityMapper(TaskDto.class, TaskEntity.class);
    }

    @PostMapping(ResourcePaths.CHUNK_PATH)
    ChunkEventOutputDto createChunkEvent(@RequestBody ChunkEventInputDto chunkEventInput) {
        //need to save to DB, and also update the recent list of things...
        return new ChunkEventOutputDto();
    }

    @GetMapping(ResourcePaths.CHUNK_PATH)
    List<ChunkEventOutputDto> getChunks(@RequestParam("from_date") String fromDate, @RequestParam("to_date") String toDate) {
        return new ArrayList<ChunkEventOutputDto>();
    }

    @GetMapping(ResourcePaths.CHUNK_PATH + ResourcePaths.RECENT_PATH)
    List<ChunkEventOutputDto> getRecentChunks() {
        return new ArrayList<ChunkEventOutputDto>();
    }
}
