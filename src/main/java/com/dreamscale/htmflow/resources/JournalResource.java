package com.dreamscale.htmflow.resources;

import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.api.journal.RecentJournalDto;
import com.dreamscale.htmflow.api.journal.TaskReferenceInputDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.project.RecentTasksSummaryDto;
import com.dreamscale.htmflow.core.domain.OrganizationMemberEntity;
import com.dreamscale.htmflow.core.mapper.DateTimeAPITranslator;
import com.dreamscale.htmflow.core.security.RequestContext;
import com.dreamscale.htmflow.core.service.JournalService;
import com.dreamscale.htmflow.core.service.OrganizationService;
import com.dreamscale.htmflow.core.service.RecentActivityService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping(ResourcePaths.ENTRY_PATH + ResourcePaths.INTENTION_PATH)
    JournalEntryDto createIntention(@RequestBody IntentionInputDto intentionInput) {
        RequestContext context = RequestContext.get();
        return journalService.createIntention(context.getMasterAccountId(), intentionInput);
    }

    /**
     * Get all recent Intentions in the Journal, ordered by time descending,
     * either for the current user (if member not provided), or for another memberId within the org
     * Defaults to providing the most recent 20 Journal entries, but a specific limit can also be provided
     */
    @GetMapping(ResourcePaths.ENTRY_PATH + ResourcePaths.RECENT_PATH)
    RecentJournalDto getRecentJournalForMember(@RequestParam("member") Optional<String> memberId, @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();

        Integer effectiveLimit = getEffectiveLimit(limit);

        List<JournalEntryDto> journalEntries;

        if (memberId.isPresent()) {
            journalEntries = journalService.getRecentIntentionsForMember(context.getMasterAccountId(), UUID.fromString(memberId.get()), effectiveLimit);
        } else {
            journalEntries = journalService.getRecentIntentions(context.getMasterAccountId(), effectiveLimit);
        }

        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());
        RecentTasksSummaryDto recentActivity = recentActivityService.getRecentTasksByProject(memberEntity.getOrganizationId(), memberEntity.getId());

        RecentJournalDto recentJournalDto = new RecentJournalDto();
        recentJournalDto.setRecentIntentions(journalEntries);
        recentJournalDto.setRecentProjects(recentActivity.getRecentProjects());
        recentJournalDto.setRecentTasksByProjectId(recentActivity.getRecentTasksByProjectId());

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
    @GetMapping(ResourcePaths.ENTRY_PATH + ResourcePaths.HISTORY_PATH)
    List<JournalEntryDto> getHistoricalEntriesBeforeDate(@RequestParam("before_date") String beforeDateStr,
                                               @RequestParam("member") Optional<String> memberId,
                                               @RequestParam("limit") Optional<Integer> limit) {
        RequestContext context = RequestContext.get();

        Integer effectiveLimit = getEffectiveLimit(limit);
        LocalDateTime beforeDate = DateTimeAPITranslator.convertToDateTime(beforeDateStr);

        if (memberId.isPresent()) {
            return journalService.getHistoricalIntentionsForMember(context.getMasterAccountId(), UUID.fromString(memberId.get()), beforeDate, effectiveLimit);
        } else {
            return journalService.getHistoricalIntentions(context.getMasterAccountId(), beforeDate, effectiveLimit);
        }
    }

    /**
     * Look up a task by name, for entry referencing into the journal.  Task to find is expected to be unique,
     * and a search to Jira able of returning any details of the task, and any other recently accessed tasks
     * in a summary
     */

    @PostMapping(ResourcePaths.TASK_PATH)
    RecentTasksSummaryDto createTaskReferenceInJournal(@RequestBody TaskReferenceInputDto taskReference) {
        RequestContext context = RequestContext.get();
        OrganizationMemberEntity memberEntity = organizationService.getDefaultMembership(context.getMasterAccountId());

        return recentActivityService.createTaskReferenceInJournal(memberEntity.getOrganizationId(), memberEntity.getId(), taskReference.getTaskName());
    }


    /**
     * Gets a mapping of all the projects and tasks recently used by the user.  By creating Intentions against
     * a project/task combination, the recent lists are automatically updated
     */

    @GetMapping(ResourcePaths.TASK_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentTasksByProject() {
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
