package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface FlowClient {

//    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.JOB_PATH + ResourcePaths.TEAM_PATH +  )
//    void addBatch(NewFlowBatch batch);
//

    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.BATCH_PATH)
    void addBatch(NewFlowBatch batch);

    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.SNIPPET_PATH)
    void addSnippet(NewSnippetEvent snippet);

    @RequestLine("GET " + ResourcePaths.FLOW_PATH + ResourcePaths.AUTH_PING_PATH)
    void authPing();

}
