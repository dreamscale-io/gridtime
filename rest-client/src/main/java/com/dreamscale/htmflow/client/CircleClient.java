package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.circle.CircleSessionInputDto;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.status.WtfStatusInputDto;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface CircleClient {

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH )
    CircleDto createNewAdhocWTFCircle(CircleSessionInputDto circleSessionInputDto);

}
