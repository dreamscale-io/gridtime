package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.journal.*;
import com.dreamscale.gridtime.api.project.RecentTasksSummaryDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JournalClient {

    //retrieve journal

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH)
    RecentJournalDto getRecentJournal();

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH + "?limit={limit}")
    RecentJournalDto getRecentJournalWithLimit(@Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + "/{username}")
    RecentJournalDto getRecentJournalForUser(@Param("username") String username);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + "/{username}" + "?limit={limit}")
    RecentJournalDto getRecentJournalForUserWithLimit(@Param("username") String username, @Param("limit") Integer limit);

    //change stuff

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH + ResourcePaths.INTENTION_PATH)
    JournalEntryDto createIntention(IntentionInputDto chunkEvent);

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH +
            ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FLAME_PATH)
    JournalEntryDto updateRetroFlameRating(@Param("id") String intentionId, FlameRatingInputDto flameRatingInput);

    @RequestLine("POST " +ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH +
            ResourcePaths.INTENTION_PATH + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.FINISH_PATH)
    JournalEntryDto finishIntention(@Param("id") String id, IntentionFinishInputDto intentionFinishInputDto);

    //recent tasks drop down support

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH + ResourcePaths.TASKREF_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksSummaryDto getRecentTaskReferencesSummary();

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.ME_PATH + ResourcePaths.TASKREF_PATH)
    RecentTasksSummaryDto createTaskReference(TaskReferenceInputDto taskReferenceDto);

    //for paging history

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + "/{username}" + ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH
            + "?before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsWithLimit(@Param("username") String username,
                                                           @Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + "/{username}" + ResourcePaths.HISTORY_PATH + ResourcePaths.FEED_PATH
            + "?before_date={beforeDate}&limit={limit}")
    List<JournalEntryDto> getHistoricalIntentionsForUserWithLimit(@Param("username") String username,
                                                                  @Param("beforeDate") String beforeDateStr, @Param("limit") Integer limit);

}
