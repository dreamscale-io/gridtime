package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamMemberOldDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TeamClient {

    @RequestLine("GET " + ResourcePaths.TEAM_PATH)
    List<TeamDto> getAllMyTeams();

    @RequestLine("GET " + ResourcePaths.TEAM_PATH + ResourcePaths.HOME_PATH)
    TeamDto getMyHomeTeam();

    @RequestLine("POST " + ResourcePaths.TEAM_PATH + ResourcePaths.HOME_PATH )
    TeamDto setMyHomeTeam(HomeTeamConfigInputDto homeTeamConfigInputDto);

    @RequestLine("POST " + ResourcePaths.TEAM_PATH + "/{teamName}")
    TeamDto createTeam(@Param("teamName") String teamName);

    @RequestLine("GET " + ResourcePaths.TEAM_PATH + "/{teamName}")
    TeamDto getTeam(@Param("teamName") String teamName);

}
