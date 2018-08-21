package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewIFMBatch;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface FlowClient {

    @RequestLine("POST " + ResourcePaths.FLOW_PATH + ResourcePaths.BATCH_PATH)
    void addIFMBatch(NewIFMBatch batch);

}
