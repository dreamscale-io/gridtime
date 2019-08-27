package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.MemberWorkStatusDto;
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
