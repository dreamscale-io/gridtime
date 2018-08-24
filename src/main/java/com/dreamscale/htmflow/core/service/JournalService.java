package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.journal.IntentionOutputDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.core.domain.*;
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
    private IntentionRepository intentionRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RecentActivityService recentActivityService;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<IntentionInputDto, IntentionEntity> intentionInputMapper;
    private DtoEntityMapper<IntentionOutputDto, IntentionEntity> intentionOutputMapper;

    @PostConstruct
    private void init() {
        intentionInputMapper = mapperFactory.createDtoEntityMapper(IntentionInputDto.class, IntentionEntity.class);
        intentionOutputMapper = mapperFactory.createDtoEntityMapper(IntentionOutputDto.class, IntentionEntity.class);
    }

    public IntentionOutputDto createIntention(UUID masterAccountId, IntentionInputDto chunkEventInput) {
        UUID organizationId = getOrganizationIdForProject(chunkEventInput.getProjectId());
        UUID memberId = getMemberIdForAccount(masterAccountId, organizationId);

        IntentionEntity intentionEntity = intentionInputMapper.toEntity(chunkEventInput);
        intentionEntity.setId(UUID.randomUUID());
        intentionEntity.setPosition(LocalDateTime.now());
        intentionEntity.setOrganizationId(organizationId);
        intentionEntity.setMemberId(memberId);

        intentionRepository.save(intentionEntity);

        recentActivityService.updateRecentProjects(intentionEntity);
        recentActivityService.updateRecentTasks(intentionEntity);

        return intentionOutputMapper.toApi(intentionEntity);
    }

    public List<IntentionOutputDto> getRecentIntentions(UUID masterAccountId) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccount(masterAccountId, organization.getId());

        List<IntentionEntity> intentionEntities = intentionRepository.findTop100ByMemberIdOrderByPosition(memberId);
        return intentionOutputMapper.toApiList(intentionEntities);
    }

    public List<IntentionOutputDto> getRecentIntentionsForMember(UUID masterAccountId, UUID memberId) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        validateMemberWithinOrg(organization, memberId);

        List<IntentionEntity> chunkEventEntities = intentionRepository.findTop100ByMemberIdOrderByPosition(memberId);
        return intentionOutputMapper.toApiList(chunkEventEntities);
    }

    public List<IntentionOutputDto> getIntentionsWithinRange(UUID masterAccountId, LocalDateTime start, LocalDateTime end) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccount(masterAccountId, organization.getId());

        //List<IntentionEntity> chunkEventEntities = intentionRepository.findChunksByMemberIdWithinRange(memberId, start, end);
        return null; //intentionOutputMapper.toApiList(chunkEventEntities);
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