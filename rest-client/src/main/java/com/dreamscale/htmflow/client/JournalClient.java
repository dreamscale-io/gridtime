package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.IntentionDto;
import com.dreamscale.htmflow.api.journal.IntentionInputDto;
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
    IntentionDto createIntention(IntentionInputDto chunkEvent);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH)
    List<IntentionDto> getRecentIntentions();

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH
            + "?limit={limit}")
    List<IntentionDto> getRecentIntentionsWithLimit(@Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH
      + "?member={memberId}")
    List<IntentionDto> getRecentIntentionsForMember(@Param("memberId") String memberId);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH
            + "?member={memberId}&limit={limit}")
    List<IntentionDto> getRecentIntentionsForMemberWithLimit(@Param("memberId") String memberId, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.HISTORY_PATH
            + "?before_date={beforeDate}&limit={limit}")
    List<IntentionDto> getHistoricalIntentionsWithLimit(@Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.HISTORY_PATH
            + "?member={memberId}&before_date={beforeDate}&limit={limit}")
    List<IntentionDto> getHistoricalIntentionsForMemberWithLimit(@Param("memberId") String memberId, @Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.TASK_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksByProjectDto getRecentTasksByProject();

}
