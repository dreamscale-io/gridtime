package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.IntentionDto;
import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class JournalService {

    @Autowired
    private IntentionRepository intentionRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RecentActivityService recentActivityService;

    @Autowired
    private TimeService timeService;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<IntentionInputDto, IntentionEntity> intentionInputMapper;
    private DtoEntityMapper<IntentionDto, IntentionEntity> intentionOutputMapper;
    private DtoEntityMapper<JournalEntryDto, JournalEntryEntity> journalEntryOutputMapper;

    @PostConstruct
    private void init() {
        intentionInputMapper = mapperFactory.createDtoEntityMapper(IntentionInputDto.class, IntentionEntity.class);
        intentionOutputMapper = mapperFactory.createDtoEntityMapper(IntentionDto.class, IntentionEntity.class);
        journalEntryOutputMapper = mapperFactory.createDtoEntityMapper(JournalEntryDto.class, JournalEntryEntity.class);
    }

    public JournalEntryDto createIntention(UUID masterAccountId, IntentionInputDto intentionInputDto) {
        UUID organizationId = getOrganizationIdForProject(intentionInputDto.getProjectId());
        UUID memberId = getMemberIdForAccountAndValidate(masterAccountId, organizationId);

        IntentionEntity intentionEntity = intentionInputMapper.toEntity(intentionInputDto);
        intentionEntity.setId(UUID.randomUUID());
        intentionEntity.setPosition(timeService.now());
        intentionEntity.setOrganizationId(organizationId);
        intentionEntity.setMemberId(memberId);

        intentionRepository.save(intentionEntity);

        recentActivityService.updateRecentProjects(intentionEntity);
        recentActivityService.updateRecentTasks(intentionEntity);

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    public List<JournalEntryDto> getRecentIntentions(UUID masterAccountId, int limit) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccountAndValidate(masterAccountId, organization.getId());

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdWithLimit(memberId, limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getRecentIntentionsForMember(UUID masterAccountId, UUID memberId, int limit) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        validateMemberWithinOrg(organization, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdWithLimit(memberId, limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getHistoricalIntentions(UUID masterAccountId, LocalDateTime beforeDate, Integer limit) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        UUID memberId = getMemberIdForAccountAndValidate(masterAccountId, organization.getId());

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdBeforeDateWithLimit(memberId, Timestamp.valueOf(beforeDate), limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getHistoricalIntentionsForMember(UUID masterAccountId, UUID memberId, LocalDateTime beforeDate, Integer limit) {
        OrganizationDto organization = organizationService.getDefaultOrganization(masterAccountId);
        validateMemberWithinOrg(organization, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdBeforeDateWithLimit(memberId, Timestamp.valueOf(beforeDate), limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    private UUID getMemberIdForAccountAndValidate(UUID masterAccountId, UUID organizationId) {
        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndMasterAccountId(organizationId, masterAccountId);
        if (memberEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found");
        } else {
            return memberEntity.getId();
        }
    }

    private UUID getOrganizationIdForProject(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Project not found");
        } else {
            return projectEntity.getOrganizationId();
        }
    }

    private void validateMemberWithinOrg(OrganizationDto organization, UUID memberId) {
        OrganizationMemberEntity otherMember = organizationMemberRepository.findById(memberId);
        if (otherMember == null || !otherMember.getOrganizationId().equals(organization.getId())) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership not found in organization");
        }
    }


}