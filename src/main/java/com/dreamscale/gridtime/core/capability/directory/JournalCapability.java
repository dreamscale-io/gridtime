package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.journal.*;
import com.dreamscale.gridtime.api.project.CreateTaskInputDto;
import com.dreamscale.gridtime.api.project.ProjectDto;
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto;
import com.dreamscale.gridtime.api.project.TaskDto;
import com.dreamscale.gridtime.api.spirit.ActiveLinksNetworkDto;
import com.dreamscale.gridtime.api.spirit.SpiritLinkDto;
import com.dreamscale.gridtime.core.capability.active.RecentActivityManager;
import com.dreamscale.gridtime.core.capability.operator.TeamCircuitOperator;
import com.dreamscale.gridtime.core.domain.flow.FinishStatus;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.json.Member;
import com.dreamscale.gridtime.core.domain.member.json.PairingMemberList;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.operator.TorchieNetworkOperator;
import com.dreamscale.gridtime.core.service.GridClock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
public class JournalCapability {

    @Autowired
    private IntentionRepository intentionRepository;

    @Autowired
    private JournalEntryRepository journalEntryRepository;

    @Autowired
    private TaskSwitchEventRepository taskSwitchEventRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private JournalLinkEventRepository journalLinkEventRepository;

    @Autowired
    private OrganizationCapability organizationMembership;

    @Autowired
    private RecentActivityManager recentActivityManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TorchieNetworkOperator torchieNetworkOperator;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private TeamCircuitOperator teamCircuitOperator;


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

        ActiveLinksNetworkDto activeLinksNetwork = torchieNetworkOperator.getActiveLinksNetwork(organizationId, memberId);

        boolean isLinked = false;
        if (!activeLinksNetwork.isEmpty()) {
            isLinked = true;
        }

        IntentionEntity myIntention = createIntentionAndGrantXPForMember(organizationId, memberId, intentionInputDto, isLinked);

        if (isLinked) {
            createJournalLinks(myIntention, memberId, activeLinksNetwork);
        }

        for (SpiritLinkDto spiritLink : activeLinksNetwork.getSpiritLinks()) {
            IntentionEntity otherIntention = createIntentionAndGrantXPForMember(organizationId, spiritLink.getFriendSpiritId(), intentionInputDto, isLinked);
            createJournalLinks(otherIntention, spiritLink.getFriendSpiritId(), activeLinksNetwork);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(myIntention.getId());
        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    public LocalDateTime getDateOfFirstIntention(UUID memberId) {
        LocalDateTime dateOfIntention = null;

        JournalEntryEntity firstEntry = journalEntryRepository.findFirstIntentionByMemberId(memberId);
        if (firstEntry != null) {
            dateOfIntention = firstEntry.getPosition();
        }

        return dateOfIntention;
    }

    private void createJournalLinks(IntentionEntity myIntention, UUID memberId, ActiveLinksNetworkDto activeLinksNetwork)  {

        JournalLinkEventEntity journalLinkEntity = new JournalLinkEventEntity();
        journalLinkEntity.setId(UUID.randomUUID());
        journalLinkEntity.setIntentionId(myIntention.getId());
        journalLinkEntity.setMemberId(memberId);
        journalLinkEntity.setLinkedMembers(translateLinkedMembersToJson(memberId, activeLinksNetwork));

        journalLinkEventRepository.save(journalLinkEntity);

    }

    private String translateLinkedMembersToJson(UUID orientFromMember, ActiveLinksNetworkDto activeLinksNetwork)  {
        List<Member> members = new ArrayList<>();

        for (SpiritLinkDto spiritLink : activeLinksNetwork.getSpiritLinks()) {
            if (!spiritLink.getFriendSpiritId().equals(orientFromMember)) {
                members.add(new Member(spiritLink.getFriendSpiritId().toString(), spiritLink.getName()));
            }
        }
        if (!orientFromMember.equals(activeLinksNetwork.getMyId())) {
            members.add(new Member(activeLinksNetwork.getMyId().toString(), activeLinksNetwork.getMyName()));
        }

        String json = null;
        try {
            json = jsonMapper.writeValueAsString(new PairingMemberList(members));
        } catch (JsonProcessingException e) {
            log.error("Unable to serialize JSON: "+members);
        }
        return json;
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

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        torchieNetworkOperator.grantXP(organizationId, memberId, memberId, now, nanoTime, 10);

        IntentionEntity lastIntention = closeLastIntention(memberId, now);
        if (lastIntention == null || (!lastIntention.getTaskId().equals(intentionInputDto.getTaskId()))) {
            TaskSwitchEventEntity taskSwitchEventEntity =
                    createTaskSwitchJournalEntry(organizationId, memberId, now, intentionInputDto);
            taskSwitchEventRepository.save(taskSwitchEventEntity);
        }

        IntentionEntity intentionEntity = intentionInputMapper.toEntity(intentionInputDto);
        intentionEntity.setId(UUID.randomUUID());
        intentionEntity.setPosition(now);
        intentionEntity.setOrganizationId(organizationId);
        intentionEntity.setLinked(isLinked);
        intentionEntity.setMemberId(memberId);
        intentionRepository.save(intentionEntity);

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());
        JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

        teamCircuitOperator.notifyTeamOfIntention(organizationId, memberId, now, nanoTime, journalEntryDto);

        recentActivityManager.updateRecentProjects(intentionEntity);
        recentActivityManager.updateRecentTasks(intentionEntity, now, nanoTime);

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
        organizationMembership.validateMemberWithinOrgByMemberId(organizationId, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdWithLimit(memberId, limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getHistoricalIntentionsForUser(UUID organizationId, String username, LocalDateTime beforeDate, Integer limit) {
        UUID memberId = organizationMembership.getMemberIdForUser(organizationId, username);

        organizationMembership.validateMemberWithinOrgByMemberId(organizationId, memberId);

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
        ActiveLinksNetworkDto spiritNetwork = torchieNetworkOperator.getActiveLinksNetwork(organizationId, memberId);
        for (SpiritLinkDto spiritLink : spiritNetwork.getSpiritLinks()) {
            List<IntentionEntity> lastIntentionList = intentionRepository.findByMemberIdWithLimit(spiritLink.getFriendSpiritId(), 1);
            if (lastIntentionList.size() > 0) {
                updateFinishStatus(organizationId, spiritLink.getFriendSpiritId(), lastIntentionList.get(0).getId(), finishStatus);
            }

        }
    }

    private JournalEntryDto updateFinishStatus(UUID organizationId, UUID memberId, UUID intentionId, FinishStatus finishStatus) {
        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        if (intentionEntity != null) {
            validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
            validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

            intentionEntity.setFinishStatus(finishStatus.name());
            intentionEntity.setFinishTime(gridClock.now());

            intentionRepository.save(intentionEntity);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionId);

        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }


    public RecentJournalDto getJournalForUser(UUID organizationId, String username, Integer effectiveLimit) {

        UUID otherMemberId = organizationMembership.getMemberIdForUser(organizationId, username);

        organizationMembership.validateMemberWithinOrgByMemberId(organizationId, otherMemberId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, otherMemberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityManager.getRecentTasksByProject(organizationId, otherMemberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;

    }

    public RecentJournalDto getJournalForSelf(UUID organizationId, UUID memberId, Integer effectiveLimit) {

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, memberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = recentActivityManager.getRecentTasksByProject(organizationId, memberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;
    }


    public ProjectDto createProject(UUID organizationId, UUID invokingMemberId, String name) {
        return null;
    }

    public TaskDto createTask(UUID organizationId, UUID invokingMemberId, UUID projectId, CreateTaskInputDto taskInputDto) {
        return null;
    }
}