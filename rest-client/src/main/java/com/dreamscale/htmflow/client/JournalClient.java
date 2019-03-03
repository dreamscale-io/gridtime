package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.*;
import com.dreamscale.htmflow.api.project.RecentTasksSummaryDto;
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

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FLAME_PATH)
    JournalEntryDto updateRetroFlameRating(@Param("id") String intentionId, FlameRatingInputDto flameRatingInput);

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.WTF_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FLAME_PATH)
    JournalEntryDto updateWTFCircleFlameRating(@Param("id") String circleContextId, FlameRatingInputDto flameRatingInput);

    @RequestLine("POST " +ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FINISH_PATH)
    JournalEntryDto finishIntention(@Param("id") String id, IntentionFinishInputDto intentionFinishInputDto);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH)
    RecentJournalDto getRecentJournal();

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH
            + "?limit={limit}")
    RecentJournalDto getRecentJournalWithLimit(@Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH
      + "?member={memberId}")
    RecentJournalDto getRecentJournalForMember(@Param("memberId") String memberId);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH
            + "?member={memberId}&limit={limit}")
    RecentJournalDto getRecentJournalForMemberWithLimit(@Param("memberId") String memberId, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH
            + "?before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsWithLimit(@Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH
            + "?member={memberId}&before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsForMemberWithLimit(@Param("memberId") String memberId, @Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);


    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.TASKREF_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentTaskReferencesSummary();

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.TASKREF_PATH)
    RecentTasksSummaryDto createTaskReference(TaskReferenceInputDto taskReferenceDto);


}
