package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.IntentionInputDto;
import com.dreamscale.htmflow.api.journal.IntentionOutputDto;
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
    IntentionOutputDto createIntention(IntentionInputDto chunkEvent);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH
            + "?from_date={fromDate}&to_date={toDate}")
    List<IntentionOutputDto> getIntentions(@Param("fromDate") String fromDate, @Param("toDate") String toDate);

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH)
    List<IntentionOutputDto> getRecentIntentions();

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.INTENTION_PATH + ResourcePaths.RECENT_PATH
      + "?member={memberId}")
    List<IntentionOutputDto> getRecentIntentionsForMember(@Param("memberId") String memberId);

    /*
    @RequestLine("POST /somepath")
    void createThingie(Thingie thingi]e);

    @RequestLine("GET /otherpath/{pathParam}?queryarg={someArg}")
    Thingie getThingieWithQueyrArg(@Param("pathParam") String pathParam, @Param("someArg") String queryArg);
    */
}
