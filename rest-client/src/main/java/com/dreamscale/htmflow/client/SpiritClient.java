package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface SpiritClient {

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.XP_PATH)
    XPSummaryDto getLatestXP();

}
