package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.journal.*;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DateTimeAPITranslator;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.capability.directory.JournalCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.active.RecentActivityManager;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {


    @Autowired
    private JournalCapability journalCapability;

    @Autowired
    private OrganizationCapability organizationCapability;

    @Autowired
    private RecentActivityManager recentActivityManager;

    private static final Integer DEFAULT_LIMIT = 100;

    /**
     * Create a new Intention in the user's Journal
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.INTENTION_PATH )
    JournalEntryDto createNewIntention(@RequestBody IntentionInputDto intentionInput) {
        RequestContext context = RequestContext.get();
        log.info("createNewIntention, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return journalCapability.createIntention(invokingMember.getOrganizationId(), invokingMember.getId(), intentionInput);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROJECT_PATH  )
    ProjectDto createProject(@RequestBody CreateProjectInputDto projectInputDto) {
        RequestContext context = RequestContext.get();
        log.info("createProject, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return journalCapability.createProject(invokingMember.getOrganizationId(), invokingMember.getId(), projectInputDto.getName());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.PROJECT_PATH + "/{projectId}" + ResourcePaths.TASK_PATH  )
    TaskDto createTask(@PathVariable("projectId") String projectIdStr, @RequestBody CreateTaskInputDto taskInputDto) {

        RequestContext context = RequestContext.get();
        log.info("createTask, user={}", context.getRootAccountId());

        UUID projectId = UUID.fromString(projectIdStr);

        OrganizationMemberEntity invokingMember = organizationCapability.getActiveMembership(context.getRootAccountId());

        return journalCapability.createTask(invokingMember.getOrganizationId(), invokingMember.getId(), projectId, taskInputDto);
    }

    /**
     * Gets an overview of all the recent projects and tasks used in the journal
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentProjectsAndTasks() {
        RequestContext context = RequestContext.get();
        log.info("getRecentProjectsAndTasks, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        return recentActivityManager.getRecentTasksByProject(memberEntity.getOrganizationId(), memberEntity.getId());
    }


    /**
     * Annotate the Intention with a flame rating
     * @param intentionId
     * @param flameRatingInputDto
     * @return JournalEntryDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FLAME_PATH)
    JournalEntryDto updateRetroFlameRating(@PathVariable("id") String intentionId, @RequestBody FlameRatingInputDto flameRatingInputDto) {
        RequestContext context = RequestContext.get();
        log.info("updateRetroFlameRating, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        return journalCapability.saveFlameRating(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId), flameRatingInputDto);
    }

    /**
     * Mark the Intention with a FinishStatus immediately, rather than closing by starting a new Intention
     * @param intentionId
     * @param intentionRefInputDto
     * @return JournalEntryDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FINISH_PATH)
    JournalEntryDto finishIntention(@PathVariable("id") String intentionId, @RequestBody IntentionFinishInputDto intentionRefInputDto) {
        RequestContext context = RequestContext.get();
        log.info("finishIntention, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        if (FinishStatus.done.equals(intentionRefInputDto.getFinishStatus())) {
            return journalCapability.finishIntention(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId));
        } else if (FinishStatus.aborted.equals(intentionRefInputDto.getFinishStatus())) {
            return journalCapability.abortIntention(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId));
        } else {
            throw new BadRequestException(ValidationErrorCodes.INVALID_FINISH_STATUS, "Invalid finish status");
        }
    }

    /**
     * Get an overview of the recent Intentions in the Journal, ordered by time descending,
     * either for the current user (if member not provided), or for another memberId within the org
     * Defaults to providing the most recent 20 Journal entries, but a specific limit can also be provided
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH)
    RecentJournalDto getRecentJournal( @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();
        log.info("getRecentJournal, user={}", context.getRootAccountId());


        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        Integer effectiveLimit = getEffectiveLimit(limit);

        return journalCapability.getJournalForSelf(memberEntity.getOrganizationId(), memberEntity.getId(), effectiveLimit);
    }


    /**
     * Get an overview of the recent Intentions in the Journal, ordered by time descending,
     * either for the current user (if member not provided), or for another memberId within the org
     * Defaults to providing the most recent 20 Journal entries, but a specific limit can also be provided
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{username}")
    RecentJournalDto getRecentJournalForUser(@PathVariable("username") String username, @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();
        log.info("getRecentJournalForUser, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        Integer effectiveLimit = getEffectiveLimit(limit);

        return journalCapability.getJournalForUser(memberEntity.getOrganizationId(), username, effectiveLimit);

    }

    /**
     * Get historical Intentions from the Journal before a specific date, ordered by time descending.
     * Can be retrieved for the current user (if member not provided), or for another memberId within the org
     * @param beforeDateStr yyyyMMdd_HHmmss
     * @param username optional member within organization
     * @param limit number of records to retrieve
     * @return List<IntentionDto>
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{username}" + ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH)
    List<JournalEntryDto> getHistoricalIntentionsFeedBeforeDate(
            @PathVariable("username") String username,
            @RequestParam("before_date") String beforeDateStr,
            @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();
        log.info("getHistoricalIntentionsFeedBeforeDate, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        Integer effectiveLimit = getEffectiveLimit(limit);
        LocalDateTime beforeDate = DateTimeAPITranslator.convertToDateTime(beforeDateStr);

        return journalCapability.getHistoricalIntentionsForUser(memberEntity.getOrganizationId(), username, beforeDate, effectiveLimit);

    }

    /**
     * Look up a task by name, for entry referencing into the journal.  Task to find is expected to be unique,
     * and a search to Jira able of returning any details of the task, and any other recently accessed tasks
     * in a summary
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.ME_PATH + ResourcePaths.TASKREF_PATH)
    RecentTasksSummaryDto createTaskReferenceInJournal(@RequestBody TaskReferenceInputDto taskReference) {

        RequestContext context = RequestContext.get();
        log.info("createTaskReferenceInJournal, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        return recentActivityManager.createTaskReferenceInJournal(memberEntity.getOrganizationId(), memberEntity.getId(), taskReference.getTaskName());
    }


    /**
     * Gets a mapping of all the projects and tasks recently used by the user.  By creating Intentions against
     * a project/task combination, the recent lists are automatically updated
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.ME_PATH + ResourcePaths.TASKREF_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentTaskReferencesSummary() {
        RequestContext context = RequestContext.get();
        log.info("getRecentTaskReferencesSummary, user={}", context.getRootAccountId());

        OrganizationMemberEntity memberEntity = organizationCapability.getActiveMembership(context.getRootAccountId());

        return recentActivityManager.getRecentTasksByProject(memberEntity.getOrganizationId(), memberEntity.getId());
    }


    private Integer getEffectiveLimit(Optional<Integer> limit) {
        Integer effectiveLimit = DEFAULT_LIMIT;
        if (limit.isPresent()) {
            effectiveLimit = limit.get();
        }
        return effectiveLimit;
    }



}
