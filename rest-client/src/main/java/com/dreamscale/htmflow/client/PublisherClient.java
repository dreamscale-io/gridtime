package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.AccountActivationDto;
import com.dreamscale.htmflow.api.account.ActivationCodeDto;
import com.dreamscale.htmflow.api.account.ConnectionStatusDto;
import com.dreamscale.htmflow.api.account.HeartbeatDto;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.batch.NewIFMBatch;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface PublisherClient {

    @RequestLine("POST " + ResourcePaths.PUBLISHER_PATH + ResourcePaths.BATCH_PATH)
    void addIFMBatch(NewIFMBatch batch);

}
