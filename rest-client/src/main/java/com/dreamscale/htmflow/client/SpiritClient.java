package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.batch.NewFlowBatch;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import com.dreamscale.htmflow.api.project.TaskInputDto;
import com.dreamscale.htmflow.api.status.WtfStatusInputDto;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface SpiritClient {

    @RequestLine("GET " + ResourcePaths.SPIRIT_PATH + ResourcePaths.XP_PATH)
    XPSummaryDto getLatestXP();

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH +  ResourcePaths.STATUS_PATH + ResourcePaths.WTF_PATH)
    TeamMemberWorkStatusDto pushWTFStatus(WtfStatusInputDto wtfStatusInputDto);

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH +  ResourcePaths.STATUS_PATH + ResourcePaths.YAY_PATH)
    TeamMemberWorkStatusDto resolveWithYay();

    @RequestLine("POST " + ResourcePaths.SPIRIT_PATH +  ResourcePaths.STATUS_PATH + ResourcePaths.ABORT_PATH)
    TeamMemberWorkStatusDto resolveWithAbort();


}
