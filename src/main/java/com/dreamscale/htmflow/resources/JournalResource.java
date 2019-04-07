package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.*;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.project.RecentTasksSummaryDto;
import com.dreamscale.htmflow.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DateTimeAPITranslator;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.JournalService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.RecentActivityService;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = ResourcePaths.JOURNAL_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class JournalResource {


    @Autowired
    private JournalService journalService;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private RecentActivityService recentActivityService;

    private static final Integer DEFAULT_LIMIT = 100;

    /**
     * Create a new Intention in the user's Journal
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INTENTION_PATH )
    JournalEntryDto createNewIntention(@RequestBody IntentionInputDto intentionInput) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return journalService.createIntention(memberEntity.getOrganizationId(), memberEntity.getId(), intentionInput);
    }


    /**
     * Annotate the Intention with a flame rating
     * @param intentionId
     * @param flameRatingInputDto
     * @return JournalEntryDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FLAME_PATH)
    JournalEntryDto updateRetroFlameRating(@PathVariable("id") String intentionId, @RequestBody FlameRatingInputDto flameRatingInputDto) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return journalService.saveFlameRating(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId), flameRatingInputDto);
    }

    /**
     * Mark the Intention with a FinishStatus immediately, rather than closing by starting a new Intention
     * @param intentionId
     * @param intentionRefInputDto
     * @return JournalEntryDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FINISH_PATH)
    JournalEntryDto finishIntention(@PathVariable("id") String intentionId, @RequestBody IntentionFinishInputDto intentionRefInputDto) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());


        if (FinishStatus.done.equals(intentionRefInputDto.getFinishStatus())) {
            return journalService.finishIntention(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId));
        } else if (FinishStatus.aborted.equals(intentionRefInputDto.getFinishStatus())) {
            return journalService.abortIntention(memberEntity.getOrganizationId(), memberEntity.getId(), UUID.fromString(intentionId));
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
    @GetMapping()
    RecentJournalDto getRecentJournalForMember(@RequestParam("member") Optional<String> memberId, @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        Integer effectiveLimit = getEffectiveLimit(limit);

        RecentJournalDto recentJournalDto;

        if (memberId.isPresent()) {
            recentJournalDto = journalService.getJournalForMember(memberEntity.getOrganizationId(), UUID.fromString(memberId.get()), effectiveLimit);
        } else {
            recentJournalDto = journalService.getJournalForSelf(memberEntity.getOrganizationId(), memberEntity.getId(), effectiveLimit);
        }

        return recentJournalDto;
    }

    /**
     * Get historical Intentions from the Journal before a specific date, ordered by time descending.
     * Can be retrieved for the current user (if member not provided), or for another memberId within the org
     * @param beforeDateStr yyyyMMdd_HHmmss
     * @param memberId optional member within organization
     * @param limit number of records to retrieve
     * @return List<IntentionDto>
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH)
    List<JournalEntryDto> getHistoricalIntentionsFeedBeforeDate(@RequestParam("before_date") String beforeDateStr,
                                               @RequestParam("member") Optional<String> memberId,
                                               @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        Integer effectiveLimit = getEffectiveLimit(limit);
        LocalDateTime beforeDate = DateTimeAPITranslator.convertToDateTime(beforeDateStr);

        if (memberId.isPresent()) {
            return journalService.getHistoricalIntentionsForMember(memberEntity.getOrganizationId(), UUID.fromString(memberId.get()), beforeDate, effectiveLimit);
        } else {
            return journalService.getHistoricalIntentionsForMember(memberEntity.getOrganizationId(), memberEntity.getId(), beforeDate, effectiveLimit);
        }
    }

    /**
     * Look up a task by name, for entry referencing into the journal.  Task to find is expected to be unique,
     * and a search to Jira able of returning any details of the task, and any other recently accessed tasks
     * in a summary
     */

    @PostMapping(ResourcePaths.TASKREF_PATH)
    RecentTasksSummaryDto createTaskReferenceInJournal(@RequestBody TaskReferenceInputDto taskReference) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return recentActivityService.createTaskReferenceInJournal(memberEntity.getOrganizationId(), memberEntity.getId(), taskReference.getTaskName());
    }


    /**
     * Gets a mapping of all the projects and tasks recently used by the user.  By creating Intentions against
     * a project/task combination, the recent lists are automatically updated
     */

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TASKREF_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentTaskReferencesSummary() {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return recentActivityService.getRecentTasksByProject(memberEntity.getOrganizationId(), memberEntity.getId());
    }


    private Integer getEffectiveLimit(Optional<Integer> limit) {
        Integer effectiveLimit = DEFAULT_LIMIT;
        if (limit.isPresent()) {
            effectiveLimit = limit.get();
        }
        return effectiveLimit;
    }

    private UUID getDefaultOrgId() {
        RequestContext context = RequestContext.get();
        OrganizationDto org = organizationService.getDefaultOrganization(context.getMasterAccountId());
        return org.getId();
    }


}
