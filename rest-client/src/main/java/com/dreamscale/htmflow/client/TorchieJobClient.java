package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.torchie.TorchieJobStatus;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TorchieJobClient {


    @RequestLine("POST " + ResourcePaths.TORCHIE_PATH + ResourcePaths.JOB_PATH + ResourcePaths.MEMBER_PATH + "/{id}"
            + ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
    TorchieJobStatus startTorchieJobForMember(@Param("id") String memberId);

    @RequestLine("POST " + ResourcePaths.TORCHIE_PATH + ResourcePaths.JOB_PATH + ResourcePaths.TEAM_PATH + "/{id}"
            + ResourcePaths.TRANSITION_PATH + ResourcePaths.START_PATH)
    TorchieJobStatus startTorchieJobForTeam(@Param("id") String teamId);


}
