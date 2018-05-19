package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class JournalService {

    @Autowired
    private JiraConnectionFactory jiraConnectionFactory;

    @Autowired
    private JiraSyncService jiraSyncService;

    @Autowired
    private ChunkEventRepository chunkEventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ChunkEventInputDto, ChunkEventEntity> chunkInputMapper;
    private DtoEntityMapper<ChunkEventOutputDto, ChunkEventEntity> chunkOutputMapper;

    @PostConstruct
    private void init() {
        chunkInputMapper = mapperFactory.createDtoEntityMapper(ChunkEventInputDto.class, ChunkEventEntity.class);
        chunkOutputMapper = mapperFactory.createDtoEntityMapper(ChunkEventOutputDto.class, ChunkEventEntity.class);
    }


    public ChunkEventOutputDto createChunkEvent(ChunkEventInputDto chunkEventInput) {
        UUID organizationId = getOrganizationIdForProject(chunkEventInput.getProjectId());

        ChunkEventEntity chunkEventEntity = chunkInputMapper.toEntity(chunkEventInput);
        chunkEventEntity.setId(UUID.randomUUID());
        chunkEventEntity.setPosition(LocalDateTime.now());
        chunkEventEntity.setOrganizationId(organizationId);

        chunkEventRepository.save(chunkEventEntity);

        return chunkOutputMapper.toApi(chunkEventEntity);
    }

    private UUID getOrganizationIdForProject(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        return projectEntity.getOrganizationId();
    }
}