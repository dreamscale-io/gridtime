package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.organization.TeamWithMembersDto;
import com.dreamscale.gridtime.api.team.HomeTeamConfigInputDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamMemberDto;
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
    List<TeamDto> getAllTeams();

    @RequestLine("POST " + ResourcePaths.TEAM_PATH + "/{teamName}")
    TeamDto createTeam(@Param("teamName") String teamName);

    @RequestLine("GET " + ResourcePaths.TEAM_PATH + "/{teamName}")
    TeamWithMembersDto getTeam(@Param("teamName") String teamName);

    @RequestLine("GET " + ResourcePaths.TEAM_PATH + ResourcePaths.HOME_PATH)
    TeamDto getMyHomeTeam();

    @RequestLine("POST " + ResourcePaths.TEAM_PATH + ResourcePaths.HOME_PATH )
    TeamDto setMyHomeTeam(HomeTeamConfigInputDto homeTeamConfigInputDto);

    @RequestLine("POST " + ResourcePaths.TEAM_PATH  + "/{teamName}" + ResourcePaths.MEMBER_PATH + "/{userName}" )
    TeamMemberDto addMemberToTeam(@Param("teamName") String teamName, @Param("userName") String userName);

    @RequestLine("POST " + ResourcePaths.TEAM_PATH  + "/{teamName}" + ResourcePaths.MEMBER_PATH + "/{userName}" + ResourcePaths.REMOVE_PATH )
    void removeMemberFromTeam(@Param("teamName") String teamName, @Param("userName") String userName);

    @RequestLine("GET " + ResourcePaths.TEAM_PATH + ResourcePaths.MY_PATH + ResourcePaths.PARTICIPATING_PATH)
    List<TeamDto> getAllMyParticipatingTeams();

}
