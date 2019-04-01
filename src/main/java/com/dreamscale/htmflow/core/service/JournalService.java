package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.journal.*;
import com.dreamscale.htmflow.api.project.RecentTasksSummaryDto;
import com.dreamscale.htmflow.api.spirit.ActiveLinksNetworkDto;
import com.dreamscale.htmflow.api.spirit.SpiritLinkDto;
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
import java.util.ArrayList;
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
    private SpiritService spiritService;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<IntentionInputDto, IntentionEntity> intentionInputMapper;
    private DtoEntityMapper<JournalEntryDto, JournalEntryEntity> journalEntryOutputMapper;

    @PostConstruct
    private void init() {
        intentionInputMapper = mapperFactory.createDtoEntityMapper(IntentionInputDto.class, IntentionEntity.class);
        journalEntryOutputMapper = mapperFactory.createDtoEntityMapper(JournalEntryDto.class, JournalEntryEntity.class);
    }

    public JournalEntryDto createIntention(UUID organizationId, UUID memberId, IntentionInputDto intentionInputDto) {
        UUID organizationIdForProject = getOrganizationIdForProject(intentionInputDto.getProjectId());

        validateMemberOrgMatchesProjectOrg(organizationId, organizationIdForProject);

        List<UUID> membersToMulticast = lookupMulticastMemberIds(organizationId, memberId);
        boolean isLinked = false;
        if (membersToMulticast.size() > 0) {
            isLinked = true;
        }

        IntentionEntity myIntention = createIntentionAndGrantXPForMember(organizationId, memberId, intentionInputDto, isLinked);

        for (UUID memberForCreation : membersToMulticast) {
            createIntentionAndGrantXPForMember(organizationId, memberForCreation, intentionInputDto, isLinked);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(myIntention.getId());
        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    private List<UUID> lookupMulticastMemberIds(UUID organizationId, UUID memberId) {
        List<UUID> membersToMulticast = new ArrayList<>();

        ActiveLinksNetworkDto activeLinksNetwork = spiritService.getActiveLinksNetwork(organizationId, memberId);
        if (!activeLinksNetwork.isEmpty()) {

            for (SpiritLinkDto link : activeLinksNetwork.getSpiritLinks()) {
                membersToMulticast.add(link.getFriendSpiritId());
            }
        }

        return membersToMulticast;
    }

    private void validateMemberOrgMatchesProjectOrg(UUID organizationId, UUID organizationIdForProject) {

        if (organizationId == null || !organizationId.equals(organizationIdForProject)) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "project org doesn't match member org");
        }

    }

    private void validateMemberIdMatchesIntentionMemberId(UUID memberId, UUID memberIdForIntention) {

        if (memberId == null || !memberId.equals(memberIdForIntention)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "member from request doesn't match member from the intention");
        }

    }

    private IntentionEntity createIntentionAndGrantXPForMember(UUID organizationId, UUID memberId, IntentionInputDto intentionInputDto, boolean isLinked) {
        spiritService.grantXP(organizationId, memberId, 10);

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
        intentionEntity.setLinked(isLinked);
        intentionEntity.setMemberId(memberId);

        intentionRepository.save(intentionEntity);

        recentActivityService.updateRecentProjects(intentionEntity);
        recentActivityService.updateRecentTasks(intentionEntity);

        return intentionEntity;


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

    public List<JournalEntryDto> getRecentIntentionsForMember(UUID organizationId, UUID memberId, int limit) {
        organizationService.validateMemberWithinOrgByMemberId(organizationId, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdWithLimit(memberId, limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getHistoricalIntentionsForMember(UUID organizationId, UUID memberId, LocalDateTime beforeDate, Integer limit) {
        organizationService.validateMemberWithinOrgByMemberId(organizationId, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdBeforeDateWithLimit(memberId, Timestamp.valueOf(beforeDate), limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }


    private UUID getOrganizationIdForProject(UUID projectId) {
        ProjectEntity projectEntity = projectRepository.findById(projectId);
        if (projectEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_PROJECT_REFERENCE, "Project not found");
        } else {
            return projectEntity.getOrganizationId();
        }
    }

    public JournalEntryDto saveFlameRating(UUID organizationId, UUID memberId, UUID intentionId, FlameRatingInputDto flameRatingInputDto) {
        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
        validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

        if (flameRatingInputDto.isValid()) {
            intentionEntity.setFlameRating(flameRatingInputDto.getFlameRating());
            intentionRepository.save(intentionEntity);
        }
        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    public JournalEntryDto finishIntention(UUID organizationId, UUID memberId, UUID intentionId) {

        JournalEntryDto myJournalEntry = updateFinishStatus(organizationId, memberId, intentionId, FinishStatus.done);
        updateFinishStatusOfMultiMembers(organizationId, memberId, FinishStatus.done);

        return myJournalEntry;
    }

    public JournalEntryDto abortIntention(UUID organizationId, UUID memberId, UUID intentionId) {
        JournalEntryDto journalEntryDto = updateFinishStatus(organizationId, memberId, intentionId, FinishStatus.aborted);
        updateFinishStatusOfMultiMembers(organizationId, memberId, FinishStatus.aborted);

        return journalEntryDto;
    }

    private void updateFinishStatusOfMultiMembers(UUID organizationId, UUID memberId, FinishStatus finishStatus) {
        List<UUID> multiCastMemberIds = lookupMulticastMemberIds(organizationId, memberId);
        for (UUID multiMember : multiCastMemberIds) {

            List<IntentionEntity> lastIntentionList = intentionRepository.findByMemberIdWithLimit(multiMember, 1);
            if (lastIntentionList.size() > 0) {
                updateFinishStatus(organizationId, multiMember, lastIntentionList.get(0).getId(), finishStatus);
            }

        }
    }

    private JournalEntryDto updateFinishStatus(UUID organizationId, UUID memberId,  UUID intentionId, FinishStatus finishStatus) {
        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        if (intentionEntity != null) {
            validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
            validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

            intentionEntity.setFinishStatus(finishStatus.name());
            intentionEntity.setFinishTime(timeService.now());

            intentionRepository.save(intentionEntity);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionId);

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }


    public RecentJournalDto getJournalForMember(UUID organizationId, UUID otherMemberId, Integer effectiveLimit) {

        organizationService.validateMemberWithinOrgByMemberId(organizationId, otherMemberId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, otherMemberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityService.getRecentTasksByProject(organizationId, otherMemberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;

    }

    public RecentJournalDto getJournalForSelf(UUID organizationId, UUID memberId, Integer effectiveLimit) {

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, memberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityService.getRecentTasksByProject(organizationId, memberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;
    }
}