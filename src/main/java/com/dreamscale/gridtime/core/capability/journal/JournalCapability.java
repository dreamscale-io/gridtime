package com.dreamscale.gridtime.core.capability.journal;

import com.dreamscale.gridtime.api.journal.*;
import com.dreamscale.gridtime.api.organization.OrganizationDto;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.api.spirit.ActiveLinksNetworkDto;
import com.dreamscale.gridtime.api.spirit.SpiritLinkDto;
import com.dreamscale.gridtime.core.capability.active.RecentActivityManager;
import com.dreamscale.gridtime.core.capability.circuit.TeamCircuitOperator;
import com.dreamscale.gridtime.core.capability.membership.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.membership.TeamCapability;
import com.dreamscale.gridtime.core.domain.active.RecentTaskEntity;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
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
    private OrganizationCapability organizationCapability;

    @Autowired
    private RecentActivityManager recentActivityManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private TorchieNetworkOperator torchieNetworkOperator;

    @Autowired
    private ProjectCapability projectCapability;

    @Autowired
    private TaskCapability taskCapability;

    @Autowired
    private TeamCapability teamCapability;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private TeamCircuitOperator teamCircuitOperator;

    @Autowired
    private WtfJournalLinkRepository wtfJournalLinkRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<IntentionInputDto, IntentionEntity> intentionInputMapper;
    private DtoEntityMapper<JournalEntryDto, JournalEntryEntity> journalEntryOutputMapper;

    @PostConstruct
    private void init() {
        intentionInputMapper = mapperFactory.createDtoEntityMapper(IntentionInputDto.class, IntentionEntity.class);
        journalEntryOutputMapper = mapperFactory.createDtoEntityMapper(JournalEntryDto.class, JournalEntryEntity.class);
    }


    @Transactional
    public JournalEntryDto createIntention(UUID organizationId, UUID memberId, IntentionInputDto intentionInputDto) {
        UUID organizationIdForProject = projectCapability.getOrganizationIdForTeamProject(intentionInputDto.getProjectId());

        validateMemberOrgMatchesProjectOrg(organizationId, organizationIdForProject);

        ActiveLinksNetworkDto activeLinksNetwork = torchieNetworkOperator.getActiveLinksNetwork(organizationId, memberId);

        boolean isLinked = false;
        if (!activeLinksNetwork.isEmpty()) {
            isLinked = true;
        }

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        //TODO this sequence is now more complicated, because we may have intermittent WTF, related journal entries.
        //so the WTF really needs to close when the WTF closes or is canceled, which means, I probably need to save the pointers
        //to these journal entries.  So maybe in the learning circuit... it's a one time update... this is prolly okay...

        //I've already got LearningCircuitEntity as an upstream caller,
        // to preserve the sequencing lock, passing it in as an argument,
        // so learning circuit can get stamped with journal_id, because it already exists

        //then the close previous intention, logic becomes more complex.

        //use cases:

        //Expectation, if you're in your WTF, write notes in your WTF, not your journal, you've got a link to this session in your journal
        //that forks your session into a nice little bundle, for you to reflect on later.  Start building out your knowledge books.

        //books of tags... you can pull tags into your books, and give them definitions.  Mix ideas of books together,
        // and use the ideas to drive search.

        //then we assume when you're back to writing in your journal, whatever the intention you were last working on, prior to the WTF,
        // prior to potentially several WTFs, would then be closed.  The WTF would be closed, via the workflow methods,
        // providing our core Idea Flow measurement system.

        // So Friction, we measure as a % of capacity consumption, with a specific risk profile organized into a queryable grid, organized by tags.
        // We can analyze these risk factors, then find strategies to reduce our WTFs, with data.

        // Did we succeed at reducing, lets query history.

        //so things I need to do, are

        JournalEntryDto myEntry = createIntentionAndGrantXPForMember(now, nanoTime, organizationId, memberId, intentionInputDto, isLinked);

        if (isLinked) {
            createJournalLinks(myEntry, memberId, activeLinksNetwork);
        }

        for (SpiritLinkDto spiritLink : activeLinksNetwork.getSpiritLinks()) {
            JournalEntryDto otherJournalEntry = createIntentionAndGrantXPForMember(now, nanoTime, organizationId, spiritLink.getFriendSpiritId(), intentionInputDto, isLinked);
            createJournalLinks(otherJournalEntry, spiritLink.getFriendSpiritId(), activeLinksNetwork);
        }

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(myEntry.getId());
        return journalEntryOutputMapper.toApi(journalEntryEntity);
    }

    @Transactional
    public JournalEntryDto createWelcomeMessage(LocalDateTime now, Long nanoTime,
                                                UUID organizationId, UUID memberId, String journalText) {


        ProjectDto defaultProject = projectCapability.findDefaultProject(organizationId);

        TaskDto defaultTask = taskCapability.findDefaultTaskForProject(organizationId, defaultProject.getId());

        return createIntentionAndGrantXPForMember(now, nanoTime, organizationId, memberId,
                new IntentionInputDto(journalText, defaultProject.getId(), defaultTask.getId()), false);

    }

    @Transactional
    public JournalEntryDto createWTFIntention(LocalDateTime now, Long nanoTime,
                                              UUID organizationId, UUID memberId, UUID wtfCircuitId, String journalText) {


        ProjectTaskContext projectTaskContext = determineWTFProjectTaskContext(organizationId, memberId, wtfCircuitId);

        ActiveLinksNetworkDto activeLinksNetwork = torchieNetworkOperator.getActiveLinksNetwork(organizationId, memberId);

        boolean isLinked = false;
        if (!activeLinksNetwork.isEmpty()) {
            isLinked = true;
        }

        IntentionEntity intentionEntity = new IntentionEntity();
        intentionEntity.setId(UUID.randomUUID());
        intentionEntity.setPosition(now);
        intentionEntity.setOrganizationId(organizationId);
        intentionEntity.setLinked(isLinked);
        intentionEntity.setMemberId(memberId);
        intentionEntity.setDescription(journalText);
        intentionEntity.setProjectId(projectTaskContext.getProjectId());
        intentionEntity.setTaskId(projectTaskContext.getTaskId());
        intentionRepository.save(intentionEntity);

        WtfJournalLinkEntity wtfJournalLinkEntity = new WtfJournalLinkEntity();
        wtfJournalLinkEntity.setId(UUID.randomUUID());
        wtfJournalLinkEntity.setOrganizationId(organizationId);
        wtfJournalLinkEntity.setMemberId(memberId);
        wtfJournalLinkEntity.setProjectId(projectTaskContext.getProjectId());
        wtfJournalLinkEntity.setTaskId(projectTaskContext.getTaskId());
        wtfJournalLinkEntity.setIntentionId(intentionEntity.getId());
        wtfJournalLinkEntity.setWtfCircuitId(wtfCircuitId);
        wtfJournalLinkEntity.setCreatedDate(now);

        wtfJournalLinkRepository.save(wtfJournalLinkEntity);

        entityManager.flush();

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());
        JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

        log.info("WTF STARTED ENTRY: " + journalEntryDto);
        teamCircuitOperator.notifyTeamOfIntention(organizationId, memberId, now, nanoTime, journalEntryDto);

        return journalEntryDto;
    }

    private ProjectTaskContext determineWTFProjectTaskContext(UUID organizationId, UUID memberId, UUID wtfCircuitId) {

        UUID activeProjectId = null;
        UUID activeTaskId = null;


        //first check if there is an existing established context for this WTF
        WtfJournalLinkEntity existingContext = wtfJournalLinkRepository.findLatestUnfinishedJournalLinkByMemberAndCircuit(organizationId, memberId, wtfCircuitId);

        if (existingContext != null) {
            activeProjectId = existingContext.getProjectId();
            activeTaskId = existingContext.getTaskId();
        }


        //if this is a new WTF, the project/task context is the most recent intention

        if (activeProjectId == null) {
            RecentTaskEntity mostRecentTask = recentActivityManager.lookupMostRecentTask(organizationId, memberId);

            if (mostRecentTask != null) {
                activeProjectId = mostRecentTask.getProjectId();
                activeTaskId = mostRecentTask.getTaskId();
            }
        }

        //if theres no intentions in the journal at all, use the default project

        if (activeProjectId == null) {

            ProjectDto defaultProject = projectCapability.findDefaultProject(organizationId);

            if (defaultProject != null) {
                activeProjectId = defaultProject.getId();

                TaskDto defaultTask = taskCapability.findDefaultTaskForProject(organizationId, defaultProject.getId());
                if (defaultTask != null) {
                    activeTaskId = defaultTask.getId();
                }
            }
        }

        return new ProjectTaskContext(activeProjectId, activeTaskId);
    }

    public LocalDateTime getDateOfFirstIntention(UUID memberId) {
        LocalDateTime dateOfIntention = null;

        JournalEntryEntity firstEntry = journalEntryRepository.findFirstIntentionByMemberId(memberId);
        if (firstEntry != null) {
            dateOfIntention = firstEntry.getPosition();
        }

        return dateOfIntention;
    }

    private void createJournalLinks(JournalEntryDto journalEntryDto, UUID memberId, ActiveLinksNetworkDto activeLinksNetwork) {

        JournalLinkEventEntity journalLinkEntity = new JournalLinkEventEntity();
        journalLinkEntity.setId(UUID.randomUUID());
        journalLinkEntity.setIntentionId(journalEntryDto.getId());
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

    private JournalEntryDto createIntentionAndGrantXPForMember(LocalDateTime now, Long nanoTime, UUID organizationId, UUID memberId, IntentionInputDto intentionInputDto, boolean isLinked) {

        torchieNetworkOperator.grantXP(organizationId, memberId, memberId, now, nanoTime, 10);

        IntentionEntity lastIntention = closeLastIntention(memberId, now, nanoTime);

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

        entityManager.flush();

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());
        JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

        recentActivityManager.updateRecentProjects(now, organizationId, memberId, intentionEntity.getProjectId());

        TaskDto taskDto = taskCapability.getTask(organizationId, intentionEntity.getProjectId(), memberId, intentionEntity.getTaskId());

        recentActivityManager.updateRecentTasks(now, organizationId, memberId, taskDto);

        teamCircuitOperator.notifyTeamOfIntention(organizationId, memberId, now, nanoTime, journalEntryDto);

        return journalEntryDto;

    }

    private TaskSwitchEventEntity createTaskSwitchJournalEntry(UUID organizationId, UUID memberId, LocalDateTime creationTime, IntentionInputDto intentionInputDto) {
        TaskSwitchEventEntity taskSwitchEventEntity = new TaskSwitchEventEntity();
        taskSwitchEventEntity.setId(UUID.randomUUID());
        taskSwitchEventEntity.setProjectId(intentionInputDto.getProjectId());
        taskSwitchEventEntity.setTaskId(intentionInputDto.getTaskId());
        taskSwitchEventEntity.setOrganizationId(organizationId);
        taskSwitchEventEntity.setMemberId(memberId);
        taskSwitchEventEntity.setPosition(creationTime.minusSeconds(1));

        TaskDto taskDto = taskCapability.getTask(organizationId, intentionInputDto.getProjectId(), memberId, intentionInputDto.getTaskId());
        if (taskDto != null) {
            taskSwitchEventEntity.setDescription(taskDto.getDescription());
        }

        return taskSwitchEventEntity;
    }

    private IntentionEntity closeLastIntention(UUID memberId, LocalDateTime now, Long nanoTime) {
        List<IntentionEntity> lastIntentionList = intentionRepository.findRecentByMemberIdWithoutWTFsWithLimit(memberId, 1);

        if (lastIntentionList.size() > 0) {
            IntentionEntity lastIntention = lastIntentionList.get(0);

            log.debug("CLOSING LAST INTENTION: "+ lastIntention.getDescription() + " : status: "+lastIntention.getFinishStatus());

            if (lastIntention.getFinishStatus() == null) {
                lastIntention.setFinishStatus(FinishStatus.done.name());
                lastIntention.setFinishTime(now);

                intentionRepository.save(lastIntention);

                entityManager.flush();
            }

            JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(lastIntention.getId());
            JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

            //because of the transactional caching against the view objects, this query wont contain the latest updates, so update manually
            journalEntryDto.setFinishStatus(lastIntention.getFinishStatus());
            journalEntryDto.setFinishTime(lastIntention.getFinishTime());

            teamCircuitOperator.notifyTeamOfIntentionFinished(lastIntention.getOrganizationId(), memberId, now, nanoTime, journalEntryDto);

            return lastIntention;
        }


        return null;
    }

    public List<JournalEntryDto> getRecentIntentionsForMember(UUID organizationId, UUID memberId, int limit) {
        organizationCapability.validateMemberWithinOrgByMemberId(organizationId, memberId);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdWithLimit(memberId, limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    public List<JournalEntryDto> getHistoricalIntentionsForUser(UUID organizationId, String username, LocalDateTime beforeDate, Integer limit) {
        UUID memberId = organizationCapability.getMemberIdForUser(organizationId, username);

        organizationCapability.validateMemberWithinOrgByMemberId(organizationId, memberId);

        log.debug("getHistoricalIntentionsForUser < date: "+beforeDate);

        List<JournalEntryEntity> journalEntryEntities = journalEntryRepository.findByMemberIdBeforeDateWithLimit(memberId, Timestamp.valueOf(beforeDate), limit);
        Collections.reverse(journalEntryEntities);

        return journalEntryOutputMapper.toApiList(journalEntryEntities);
    }

    @Transactional
    public JournalEntryDto saveFlameRating(UUID organizationId, UUID memberId, UUID intentionId, FlameRatingInputDto flameRatingInputDto) {
        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);

        validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
        validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

        if (flameRatingInputDto.isValid()) {
            intentionEntity.setFlameRating(flameRatingInputDto.getFlameRating());
            intentionRepository.save(intentionEntity);

            entityManager.flush();
        }
        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        //below is workaround, hibernate can return cached version without updates

        journalEntryEntity.setFlameRating(intentionEntity.getFlameRating());
        JournalEntryDto journalEntryDto = journalEntryOutputMapper.toApi(journalEntryEntity);

        teamCircuitOperator.notifyTeamOfIntentionUpdate(organizationId, memberId, now, nanoTime, journalEntryDto);

        return journalEntryDto;
    }

    @Transactional
    public JournalEntryDto finishIntention(UUID organizationId, UUID memberId, UUID intentionId) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);
        validateNotNull("intentionEntity", intentionEntity);

        JournalEntryDto myJournalEntry = updateFinishStatus(now, organizationId, memberId, intentionEntity, FinishStatus.done);
        updateFinishStatusOfMultiMembers(now, organizationId, memberId, FinishStatus.done);

        teamCircuitOperator.notifyTeamOfIntentionFinished(organizationId, memberId, now, nanoTime, myJournalEntry);

        return myJournalEntry;
    }

    @Transactional
    public JournalEntryDto finishWTFIntention(LocalDateTime now, Long nanoTime, UUID organizationId, UUID memberId, UUID circuitId) {

        WtfJournalLinkEntity journalLink = wtfJournalLinkRepository.findLatestUnfinishedJournalLinkByMemberAndCircuit(organizationId, memberId, circuitId);

        validateNotNull("journalLink", journalLink);

        IntentionEntity intentionEntity = intentionRepository.findOne(journalLink.getIntentionId());

        validateFinishStatusIsNull(intentionEntity);

        JournalEntryDto myJournalEntry = updateFinishStatus(now, organizationId, memberId, intentionEntity, FinishStatus.done);

        teamCircuitOperator.notifyTeamOfIntentionFinished(organizationId, memberId, now, nanoTime, myJournalEntry);

        return myJournalEntry;
    }

    @Transactional
    public JournalEntryDto abortWTFIntention(LocalDateTime now, Long nanoTime, UUID organizationId, UUID memberId, UUID circuitId) {

        WtfJournalLinkEntity journalLink = wtfJournalLinkRepository.findLatestUnfinishedJournalLinkByMemberAndCircuit(organizationId, memberId, circuitId);

        validateNotNull("journalLink", journalLink);

        IntentionEntity intentionEntity = intentionRepository.findOne(journalLink.getIntentionId());

        validateFinishStatusIsNull(intentionEntity);

        JournalEntryDto myJournalEntry = updateFinishStatus(now, organizationId, memberId, intentionEntity, FinishStatus.aborted);

        teamCircuitOperator.notifyTeamOfIntentionAborted(organizationId, memberId, now, nanoTime, myJournalEntry);

        return myJournalEntry;
    }


    @Transactional
    public JournalEntryDto abortIntention(UUID organizationId, UUID memberId, UUID intentionId) {
        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        IntentionEntity intentionEntity = intentionRepository.findOne(intentionId);
        validateNotNull("intentionEntity", intentionEntity);

        JournalEntryDto journalEntryDto = updateFinishStatus(now, organizationId, memberId, intentionEntity, FinishStatus.aborted);
        updateFinishStatusOfMultiMembers(now, organizationId, memberId, FinishStatus.aborted);

        teamCircuitOperator.notifyTeamOfIntentionAborted(organizationId, memberId, now, nanoTime, journalEntryDto);

        return journalEntryDto;
    }

    private void updateFinishStatusOfMultiMembers(LocalDateTime now, UUID organizationId, UUID memberId, FinishStatus finishStatus) {
        ActiveLinksNetworkDto spiritNetwork = torchieNetworkOperator.getActiveLinksNetwork(organizationId, memberId);
        for (SpiritLinkDto spiritLink : spiritNetwork.getSpiritLinks()) {
            List<IntentionEntity> lastIntentionList = intentionRepository.findRecentByMemberIdWithoutWTFsWithLimit(spiritLink.getFriendSpiritId(), 1);
            if (lastIntentionList.size() > 0) {
                updateFinishStatus(now, organizationId, spiritLink.getFriendSpiritId(), lastIntentionList.get(0), finishStatus);
            }
        }
    }

    private JournalEntryDto updateFinishStatus(LocalDateTime now, UUID organizationId, UUID memberId, IntentionEntity intentionEntity, FinishStatus finishStatus) {

        validateNotNull("intentionEntity", intentionEntity);

        validateMemberOrgMatchesProjectOrg(organizationId, intentionEntity.getOrganizationId());
        validateMemberIdMatchesIntentionMemberId(memberId, intentionEntity.getMemberId());

        validateFinishStatusIsNull(intentionEntity);

        intentionEntity.setFinishStatus(finishStatus.name());
        intentionEntity.setFinishTime(now);

        intentionRepository.save(intentionEntity);

        entityManager.flush();

        //TODO this is coming back null

        JournalEntryEntity journalEntryEntity = journalEntryRepository.findOne(intentionEntity.getId());

        journalEntryEntity.setFinishStatus(intentionEntity.getFinishStatus());
        journalEntryEntity.setFinishTime(intentionEntity.getFinishTime());

        return journalEntryOutputMapper.toApi(journalEntryEntity);

    }

    private void validateFinishStatusIsNull(IntentionEntity intentionEntity) {
        if (intentionEntity.getFinishStatus() != null) {
            throw new BadRequestException(ValidationErrorCodes.INTENTION_CANT_BE_EDITED, "Unable to edit finish status of intention already finished.");
        }
    }


    public RecentJournalDto getJournalForUser(UUID organizationId, String username, Integer effectiveLimit) {

        UUID otherMemberId = organizationCapability.getMemberIdForUser(organizationId, username);

        organizationCapability.validateMemberWithinOrgByMemberId(organizationId, otherMemberId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, otherMemberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = getRecentProjectsAndTasks(organizationId, otherMemberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;

    }

    public RecentJournalDto getJournalForSelf(UUID organizationId, UUID memberId, Integer effectiveLimit) {

        initializeJournalIfNoEntries(organizationId, memberId);

        List<JournalEntryDto> journalEntries = getRecentIntentionsForMember(organizationId, memberId, effectiveLimit);
        RecentTasksSummaryDto recentActivity = getRecentProjectsAndTasks(organizationId, memberId);

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

        return recentJournalDto;
    }

    private void initializeJournalIfNoEntries(UUID organizationId, UUID memberId) {
        IntentionEntity intentionEntity = intentionRepository.findFirstByMemberId(memberId);

        if (intentionEntity == null) {
            LocalDateTime now = gridClock.now();
            Long nanoTime = gridClock.nanoTime();

            OrganizationDto org = organizationCapability.getOrganization(organizationId);

            createWelcomeMessage(now, nanoTime, organizationId, memberId, "Welcome to the " + org.getDomainName() + " hyperspace");
        }
    }

    public RecentTasksSummaryDto getRecentProjectsAndTasks(UUID organizationId, UUID memberId) {

        RecentTasksSummaryDto recentTasksSummaryDto = new RecentTasksSummaryDto();

        List<ProjectDto> recentProjects = projectCapability.findProjectsByRecentMemberAccess(organizationId, memberId);

        List<ProjectDto> extendedProjects = Collections.emptyList();

        if (recentProjects.size() < 5) {
            extendedProjects = projectCapability.findProjectsByMemberPermission(organizationId, memberId, 5);
        }

        ProjectDto noProjectProject = projectCapability.findDefaultProject(organizationId);

        List<ProjectDto> projectDtos = combineRecentProjectsWithDefaults(recentProjects, noProjectProject, extendedProjects);

        for (ProjectDto projectDto : projectDtos) {

            List<TaskDto> recentTasks = taskCapability.findTasksByRecentMemberAccess(organizationId, memberId, projectDto.getId());

            TaskDto noTaskDefaultTask = taskCapability.findDefaultTaskForProject(organizationId, projectDto.getId());

            List<TaskDto> recentTasksWithDefaults = combineRecentTasksWithDefaults(recentTasks, noTaskDefaultTask);

            recentTasksSummaryDto.addRecentProjectTasks(projectDto, recentTasksWithDefaults);
        }

        TaskDto activeTask = taskCapability.findMostRecentTask(organizationId, memberId);

        recentTasksSummaryDto.setActiveTask(activeTask);

        return recentTasksSummaryDto;
    }


    private List<TaskDto> combineRecentTasksWithDefaults
            (List<TaskDto> recentTasks, TaskDto noTaskTask) {

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

        return new ArrayList<>(recentTaskMap.values());
    }


    private List<ProjectDto> combineRecentProjectsWithDefaults
            (List<ProjectDto> recentProjects, ProjectDto noProjectProject, List<ProjectDto> defaultProjects) {
        Map<UUID, ProjectDto> recentProjectMap = new LinkedHashMap<>();

        int numberAdded = 0;

        for (ProjectDto recentProject : recentProjects) {
            recentProjectMap.put(recentProject.getId(), recentProject);
        }

        //either add the noTaskTask as the 5th last entry, if it's not among recent
        for (ProjectDto recentProject : recentProjects) {
            recentProjectMap.put(recentProject.getId(), recentProject);
            numberAdded++;

            if (numberAdded == 4 && recentProjectMap.get(noProjectProject.getId()) == null) {
                recentProjectMap.putIfAbsent(noProjectProject.getId(), noProjectProject);
                break;
            }
        }

        for (ProjectDto defaultProject : defaultProjects) {
            if (recentProjectMap.size() < 5) {
                recentProjectMap.putIfAbsent(defaultProject.getId(), defaultProject);
            }
        }

        return new ArrayList<>(recentProjectMap.values());
    }

    public ProjectDto findOrCreateProject(UUID organizationId, UUID memberId, CreateProjectInputDto projectInputDto) {

        LocalDateTime now = gridClock.now();

        ProjectDto projectDto = projectCapability.findOrCreateProject(now, organizationId, memberId, projectInputDto);

        recentActivityManager.updateRecentProjects(now, organizationId, memberId, projectDto.getId());

        return projectDto;
    }

    public TaskDto findOrCreateTask(UUID organizationId, UUID memberId, UUID projectId, CreateTaskInputDto taskInputDto) {

        LocalDateTime now = gridClock.now();

        projectCapability.validateProjectPermission(organizationId, memberId, projectId);

        validateNotNull("task", taskInputDto.getName());

        TaskDto taskDto = taskCapability.findOrCreateTask(organizationId, memberId, projectId, taskInputDto);

        recentActivityManager.updateRecentTasks(now, organizationId, memberId, taskDto);

        return taskDto;
    }

    private void validateNotNull(String propertyName, Object property) {
        if (property == null) {
            throw new BadRequestException(ValidationErrorCodes.PROPERTY_CANT_BE_NULL, "Property " + propertyName + " cant be null");
        }
    }

    public UUID getLastActiveProjectId(UUID organizationId, UUID memberId) {

        return recentActivityManager.lookupProjectIdOfMostRecentActivity(organizationId, memberId);
    }

    @Data
    @AllArgsConstructor
    private static class ProjectTaskContext {
        private UUID projectId;
        private UUID taskId;
    }
}
