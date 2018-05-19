package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.hooks.jira.JiraConnectionFactory;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.ErrorEntity;
import org.dreamscale.exception.WebApplicationException;
import org.dreamscale.logging.LoggingLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JournalService {

    @Autowired
    private ChunkEventRepository chunkEventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationService organizationService;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<ChunkEventInputDto, ChunkEventEntity> chunkInputMapper;
    private DtoEntityMapper<ChunkEventOutputDto, ChunkEventEntity> chunkOutputMapper;

    @PostConstruct
    private void init() {
        chunkInputMapper = mapperFactory.createDtoEntityMapper(ChunkEventInputDto.class, ChunkEventEntity.class);
        chunkOutputMapper = mapperFactory.createDtoEntityMapper(ChunkEventOutputDto.class, ChunkEventEntity.class);
    }

    public ChunkEventOutputDto createChunkEvent(UUID masterAccountId, ChunkEventInputDto chunkEventInput) {
        UUID organizationId = getOrganizationIdForProject(chunkEventInput.getProjectId());
        UUID memberId = getMemberIdForAccount(masterAccountId, organizationId);

        ChunkEventEntity chunkEventEntity = chunkInputMapper.toEntity(chunkEventInput);
        chunkEventEntity.setId(UUID.randomUUID());
        chunkEventEntity.setPosition(LocalDateTime.now());
        chunkEventEntity.setOrganizationId(organizationId);
        chunkEventEntity.setMemberId(memberId);

        chunkEventRepository.save(chunkEventEntity);

        return chunkOutputMapper.toApi(chunkEventEntity);
    }

    public List<ChunkEventOutputDto> getRecentChunks(UUID masterAccountId) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccount(masterAccountId, organization.getId());

        List<ChunkEventEntity> chunkEventEntities = chunkEventRepository.findTop100ByMemberIdOrderByPosition(memberId);
        return chunkOutputMapper.toApiList(chunkEventEntities);
    }

    public List<ChunkEventOutputDto> getRecentChunksForMember(UUID masterAccountId, UUID memberId) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        validateMemberWithinOrg(organization, memberId);

        List<ChunkEventEntity> chunkEventEntities = chunkEventRepository.findTop100ByMemberIdOrderByPosition(memberId);
        return chunkOutputMapper.toApiList(chunkEventEntities);
    }

    public List<ChunkEventOutputDto> getChunksWithinRange(UUID masterAccountId, LocalDateTime start, LocalDateTime end) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccount(masterAccountId, organization.getId());

        //List<ChunkEventEntity> chunkEventEntities = chunkEventRepository.findChunksByMemberIdWithinRange(memberId, start, end);
        return null; //chunkOutputMapper.toApiList(chunkEventEntities);
    }

    private UUID getMemberIdForAccount(UUID masterAccountId, UUID organizationId) {
        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndMasterAccountId(organizationId, masterAccountId);
        if (memberEntity == null) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "membership not found", null, LoggingLevel.WARN));
        } else {
            return memberEntity.getId();
        }
    }

    private UUID getOrganizationIdForProject(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "project not found", null, LoggingLevel.WARN));
        } else {
            return projectEntity.getOrganizationId();
        }
    }

    private void validateMemberWithinOrg(OrganizationDto organization, UUID memberId) {
        OrganizationMemberEntity otherMember = organizationMemberRepository.findById(memberId);
        if (otherMember == null || !otherMember.getOrganizationId().equals(organization.getId())) {
            throw new WebApplicationException(404, new ErrorEntity("404", null, "member not found in organization", null, LoggingLevel.WARN));
        }
    }


}