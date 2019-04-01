package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.organization.MemberWorkStatusDto;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface MemberStatusClient {

    @RequestLine("GET " + ResourcePaths.STATUS_PATH + ResourcePaths.ME_PATH)
    MemberWorkStatusDto getMyCurrentStatus();

    @RequestLine("GET " + ResourcePaths.STATUS_PATH + ResourcePaths.TEAM_PATH)
    List<MemberWorkStatusDto> getStatusOfMeAndMyTeam();

}
