package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.*;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.project.RecentTasksSummaryDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.domain.flow.FinishStatus;
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
    private TaskSwitchEventRepository taskSwitchEventRepository;

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
    private TaskRepository taskRepository;

    @Autowired
    private SpiritService xpService;


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

        xpService.grantXP(organizationId, memberId, 10);

        LocalDateTime creationTime = timeService.now();

        IntentionEntity lastIntention = closeLastIntention(memberId, creationTime);
        if (lastIntention == null || (!lastIntention.getTaskId().equals(intentionInputDto.getTaskId()))) {
            TaskSwitchEventEntity taskSwitchEventEntity =
                    createTaskSwitchJournalEntry(organizationId, memberId, creationTime, intentionInputDto);
            taskSwitchEventRepository.save(taskSwitchEventEntity);
        }


        IntentionEntity intentionEntity = intentionInputMapper.toEntity(intentionInputDto);
        intentionEntity.setId(UUID.randomUUID());
        intentionEntity.setPosition(creationTime);
        intentionEntity.setOrganizationId(organizationId);
        intentionEntity.setMemberId(memberId);

        intentionRepository.save(intentionEntity);

        recentActivityService.updateRecentProjects(intentionEntity);
        recentActivityService.updateRecentTasks(intentionEntity);

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    private TaskSwitchEventEntity createTaskSwitchJournalEntry(UUID organizationId, UUID memberId, LocalDateTime creationTime, IntentionInputDto intentionInputDto) {
        TaskSwitchEventEntity taskSwitchEventEntity = new TaskSwitchEventEntity();
        taskSwitchEventEntity.setId(UUID.randomUUID());
        taskSwitchEventEntity.setProjectId(intentionInputDto.getProjectId());
        taskSwitchEventEntity.setTaskId(intentionInputDto.getTaskId());
        taskSwitchEventEntity.setOrganizationId(organizationId);
        taskSwitchEventEntity.setMemberId(memberId);
        taskSwitchEventEntity.setPosition(creationTime.minusSeconds(1));

        TaskEntity taskEntity = taskRepository.findOne(intentionInputDto.getTaskId());
        if (taskEntity != null) {
            taskSwitchEventEntity.setDescription(taskEntity.getSummary());
        }

        return taskSwitchEventEntity;
    }

    private IntentionEntity closeLastIntention(UUID memberId, LocalDateTime finishTime) {
        List<IntentionEntity> lastIntentionList = intentionRepository.findByMemberIdWithLimit(memberId, 1);

        if (lastIntentionList.size() > 0) {
            IntentionEntity lastIntention = lastIntentionList.get(0);

            if (lastIntention.getFinishStatus() == null) {
                lastIntention.setFinishStatus("done");
                lastIntention.setFinishTime(finishTime);

                intentionRepository.save(lastIntention);
            }
            return lastIntention;
        }
        return null;
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
        organizationService.validateMemberWithinOrgByMemberId(organization.getId(), memberId);

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
        organizationService.validateMemberWithinOrgByMemberId(organization.getId(), memberId);

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

    public JournalEntryDto saveFlameRating(UUID masterAccountId, UUID intentionId, FlameRatingInputDto flameRatingInputDto) {
        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        organizationService.validateMemberWithinOrg(intentionEntity.getOrganizationId(), masterAccountId);

        if (flameRatingInputDto.isValid()) {
            intentionEntity.setFlameRating(flameRatingInputDto.getFlameRating());
            intentionRepository.save(intentionEntity);
        }
        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    public JournalEntryDto finishIntention(UUID masterAccountId, UUID intentionId) {

        return updateFinishStatus(masterAccountId, intentionId, FinishStatus.done);
    }

    public JournalEntryDto abortIntention(UUID masterAccountId, UUID intentionId) {

        return updateFinishStatus(masterAccountId, intentionId, FinishStatus.aborted);
    }

    private JournalEntryDto updateFinishStatus(UUID masterAccountId, UUID intentionId, FinishStatus finishStatus) {
        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        if (intentionEntity != null) {
            organizationService.validateMemberWithinOrg(intentionEntity.getOrganizationId(), masterAccountId);

            intentionEntity.setFinishStatus(finishStatus.name());
            intentionEntity.setFinishTime(timeService.now());

            intentionRepository.save(intentionEntity);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionId);

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }


    public RecentJournalDto getJournalForMember(UUID masterAccountId, UUID otherMemberId, Integer effectiveLimit) {

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(masterAccountId);
        organizationService.validateMemberWithinOrgByMemberId(invokingMember.getOrganizationId(), otherMemberId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(masterAccountId, otherMemberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityService.getRecentTasksByProject(invokingMember.getOrganizationId(), otherMemberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;

    }

    public RecentJournalDto getJournalForSelf(UUID masterAccountId, Integer effectiveLimit) {

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(masterAccountId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(masterAccountId, invokingMember.getId(), effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityService.getRecentTasksByProject(invokingMember.getOrganizationId(), invokingMember.getId());

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;
    }
}