package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.team.TeamDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TeamClient {

    @RequestLine("GET " + ResourcePaths.TEAM_PATH)
    TeamDto getMyPrimaryTeam();

}
