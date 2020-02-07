package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.flow.batch.NewFlowBatchDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface FlowClient {

//    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.JOB_PATH + ResourcePaths.TEAM_PATH +  )
//    void addBatch(NewFlowBatchDto batch);
//

    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.INPUT_PATH + ResourcePaths.BATCH_PATH)
    void publishBatch(NewFlowBatchDto batch);

    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.INPUT_PATH + ResourcePaths.SNIPPET_PATH)
    void publishSnippet(NewSnippetEventDto snippet);

    @RequestLine("GET " + ResourcePaths.FLOW_PATH + ResourcePaths.AUTH_PING_PATH)
    void authPing();

}
