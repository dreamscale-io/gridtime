package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.IntentionDto;
import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.journal.JournalEntryDto;
import com.dreamscale.htmflow.api.journal.RecentJournalDto;
import com.dreamscale.htmflow.api.project.RecentTasksByProjectDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JournalClient {

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH)
    JournalEntryDto createIntention(IntentionInputDto chunkEvent);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.RECENT_PATH)
    RecentJournalDto getRecentJournal();

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.RECENT_PATH
            + "?limit={limit}")
    RecentJournalDto getRecentJournalWithLimit(@Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.RECENT_PATH
      + "?member={memberId}")
    RecentJournalDto getRecentJournalForMember(@Param("memberId") String memberId);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.RECENT_PATH
            + "?member={memberId}&limit={limit}")
    RecentJournalDto getRecentJournalForMemberWithLimit(@Param("memberId") String memberId, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.HISTORY_PATH
            + "?before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsWithLimit(@Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.HISTORY_PATH
            + "?member={memberId}&before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsForMemberWithLimit(@Param("memberId") String memberId, @Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.RECENT_PATH + ResourcePaths.TASK_PATH)
    RecentTasksByProjectDto getRecentTasksByProject();

}
