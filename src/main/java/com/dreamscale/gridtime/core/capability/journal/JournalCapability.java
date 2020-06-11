package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.journal.*;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.api.spirit.ActiveLinksNetworkDto;
import com.dreamscale.gridtime.api.spirit.SpiritLinkDto;
import com.dreamscale.gridtime.api.team.TeamLinkDto;
import com.dreamscale.gridtime.core.capability.active.RecentActivityManager;
import com.dreamscale.gridtime.core.capability.circuit.TeamCircuitOperator;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.flow.FinishStatus;
import com.dreamscale.gridtime.core.domain.journal.*;
import com.dreamscale.gridtime.core.domain.member.json.Member;
import com.dreamscale.gridtime.core.domain.member.json.PairingMemberList;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.circuit.TorchieNetworkOperator;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

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
    private JournalLinkEventRepository journalLinkEventRepository;

    @Autowired
    private OrganizationCapability organizationMembership;

    @Autowired
    private RecentActivityManager recentActivityManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private TorchieNetworkOperator torchieNetworkOperator;

    @Autowired
    private TeamProjectCapability teamProjectCapability;

    @Autowired
    private TeamTaskCapability teamTaskCapability;

    @Autowired
    private TeamCapability teamCapability;

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
        UUID organizationIdForProject = teamProjectCapability.getOrganizationIdForTeamProject(intentionInputDto.getProjectId());

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

    private void createJournalLinks(IntentionEntity myIntention, UUID memberId, ActiveLinksNetworkDto activeLinksNetwork) {

        JournalLinkEventEntity journalLinkEntity = new JournalLinkEventEntity();
        journalLinkEntity.setId(UUID.randomUUID());
        journalLinkEntity.setIntentionId(myIntention.getId());
        journalLinkEntity.setMemberId(memberId);
        journalLinkEntity.setLinkedMembers(translateLinkedMembersToJson(memberId, activeLinksNetwork));

        journalLinkEventRepository.save(journalLinkEntity);

    }

    private String translateLinkedMembersToJson(UUID orientFromMember, ActiveLinksNetworkDto activeLinksNetwork) {
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
            log.error("Unable to serialize JSON: " + members);
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

        recentActivityManager.updateRecentProjects(now, organizationId, memberId, intentionEntity.getProjectId());

        TaskDto taskDto = teamTaskCapability.getTeamTask(intentionEntity.getTaskId());

        recentActivityManager.updateRecentTasks(now, organizationId, memberId, taskDto);

        teamCircuitOperator.notifyTeamOfIntention(organizationId, memberId, now, nanoTime, journalEntryDto);

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

        TaskDto taskDto = teamTaskCapability.getTeamTask(intentionInputDto.getTaskId());
        if (taskDto != null) {
            taskSwitchEventEntity.setDescription(taskDto.getSummary());
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

    public JournalEntryDto saveFlameRating(UUID organizationId, UUID memberId, UUID intentionId, FlameRatingInputDto flameRatingInputDto) {
        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
        validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

        if (flameRatingInputDto.isValid()) {
            intentionEntity.setFlameRating(flameRatingInputDto.getFlameRating());
            intentionRepository.save(intentionEntity);
        }
        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

        teamCircuitOperator.notifyTeamOfIntentionUpdate(organizationId, memberId, now, nanoTime, journalEntryDto);

        return journalEntryDto;
    }

    public JournalEntryDto finishIntention(UUID organizationId, UUID memberId, UUID intentionId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        JournalEntryDto myJournalEntry = updateFinishStatus(organizationId, memberId, intentionId, FinishStatus.done);
        updateFinishStatusOfMultiMembers(organizationId, memberId, FinishStatus.done);

        teamCircuitOperator.notifyTeamOfIntentionUpdate(organizationId, memberId, now, nanoTime, myJournalEntry);

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
        RecentTasksSummaryDto recentActivity = getRecentProjectsAndTasks(organizationId, otherMemberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;

    }

    public RecentJournalDto getJournalForSelf(UUID organizationId, UUID memberId, Integer effectiveLimit) {

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, memberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = getRecentProjectsAndTasks(organizationId, memberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;
    }

    public RecentTasksSummaryDto getRecentProjectsAndTasks(UUID organizationId, UUID memberId) {

        RecentTasksSummaryDto recentTasksSummaryDto = new RecentTasksSummaryDto();

        List<ProjectDto> recentProjects = teamProjectCapability.findProjectsByRecentMemberAccess(organizationId, memberId);

        TeamLinkDto activeTeamLink = teamCapability.getMyActiveTeamLink(organizationId, memberId);

        List<ProjectDto> recentTeamProjects = teamProjectCapability.findProjectsByRecentTeamMemberAccess(organizationId, activeTeamLink.getId());
        List<ProjectDto> projectDtos = combineRecentProjectsWithDefaults(recentProjects, recentTeamProjects);

        for (ProjectDto projectDto : projectDtos) {

            List<TaskDto> recentTasks = teamTaskCapability.findTasksByRecentMemberAccess(organizationId, memberId, projectDto.getId());
            List<TaskDto> recentTeamTasks = teamTaskCapability.findTasksByRecentTeamAccess(organizationId, activeTeamLink.getId(), projectDto.getId());

            TaskDto noTaskDefaultTask = teamTaskCapability.findDefaultProjectTask(organizationId, projectDto.getId());

            List<TaskDto> recentTasksWithDefaults = combineRecentTasksWithDefaults(recentTasks, recentTeamTasks, noTaskDefaultTask);

            recentTasksSummaryDto.addRecentProjectTasks(projectDto, recentTasksWithDefaults);
        }

        TaskDto activeTask = teamTaskCapability.findMostRecentTask(organizationId, memberId);

        recentTasksSummaryDto.setActiveTask(activeTask);

        return recentTasksSummaryDto;
    }


    private List<TaskDto> combineRecentTasksWithDefaults
            (List<TaskDto> recentTasks, List<TaskDto> defaultTasks, TaskDto noTaskTask) {

        Map<UUID, TaskDto> recentTaskMap = new LinkedHashMap<>();

        int numberAdded = 0;

        //either add the noTaskTask as the 5th last entry, if it's not among recent
        for (TaskDto recentTask : recentTasks) {
            recentTaskMap.put(recentTask.getId(), recentTask);
            numberAdded++;

            if (numberAdded == 4 && recentTaskMap.get(noTaskTask.getId()) == null) {
                recentTaskMap.putIfAbsent(noTaskTask.getId(), noTaskTask);
                break;
            }
        }

        //or if there's not yet 4 entries in recent, add to the top
        if (noTaskTask != null) {
            recentTaskMap.putIfAbsent(noTaskTask.getId(), noTaskTask);
        }

        for (TaskDto defaultTask : defaultTasks) {
            if (recentTaskMap.size() < 5) {
                recentTaskMap.putIfAbsent(defaultTask.getId(), defaultTask);
            }
        }

        return new ArrayList<>(recentTaskMap.values());
    }


    private List<ProjectDto> combineRecentProjectsWithDefaults
            (List<ProjectDto> recentProjects, List<ProjectDto> defaultProjects) {
        Map<UUID, ProjectDto> recentProjectMap = new LinkedHashMap<>();

        for (ProjectDto recentProject : recentProjects) {
            recentProjectMap.put(recentProject.getId(), recentProject);
        }

        for (ProjectDto defaultProject : defaultProjects) {
            if (recentProjectMap.size() < 5) {
                recentProjectMap.putIfAbsent(defaultProject.getId(), defaultProject);
            }
        }

        return new ArrayList<>(recentProjectMap.values());
    }

    public ProjectDto findOrCreateTeamProject(UUID organizationId, UUID memberId, CreateProjectInputDto projectInputDto) {

        LocalDateTime now = gridClock.now();

        ProjectDto projectDto = teamProjectCapability.findOrCreateTeamProject(now, organizationId, memberId, projectInputDto);

        TeamLinkDto teamLink = teamCapability.getMyActiveTeamLink(organizationId, memberId);
        TaskDto defaultNoTaskTask = teamTaskCapability.createDefaultProjectTask(now, organizationId, memberId, teamLink.getId(), projectDto.getId());

        recentActivityManager.updateRecentProjects(now, organizationId, memberId, projectDto.getId());

        return projectDto;
    }

    public TaskDto findOrCreateTeamTask(UUID organizationId, UUID memberId, UUID projectId, CreateTaskInputDto taskInputDto) {

        LocalDateTime now = gridClock.now();

        TaskDto taskDto = teamTaskCapability.findOrCreateTeamTask(now, organizationId, memberId, projectId, taskInputDto);

        recentActivityManager.updateRecentTasks(now, organizationId, memberId, taskDto);

        return taskDto;
    }

    public UUID getLastActiveProjectId(UUID organizationId, UUID memberId) {

        return recentActivityManager.lookupProjectIdOfMostRecentActivity(organizationId, memberId);
    }
}