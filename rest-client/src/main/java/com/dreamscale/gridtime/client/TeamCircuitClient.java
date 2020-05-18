package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.DescriptionInputDto;
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.circuit.LearningCircuitWithMembersDto;
import com.dreamscale.gridtime.api.circuit.TagsInputDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TeamCircuitClient {


    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH )
    List<TeamCircuitDto> getTeamCircuits(@Param("teamName") String teamName);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.HOME_PATH)
    TeamCircuitDto getMyTeamCircuit();

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}")
    TeamCircuitDto getTeamCircuitByName(@Param("teamName") String teamName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}")
    TeamCircuitRoomDto createTeamCircuitRoom(@Param("teamName") String teamName, @Param("roomName") String roomName);

    @RequestLine("GET " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}")
    TeamCircuitRoomDto getTeamCircuitRoom(@Param("teamName") String teamName, @Param("roomName") String roomName);


    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CLOSE_PATH)
    TeamCircuitRoomDto closeTeamCircuitRoom(@Param("teamName") String teamName, @Param("roomName") String roomName);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" +
            ResourcePaths.PROPERTY_PATH + ResourcePaths.DESCRIPTION_PATH)
    TeamCircuitRoomDto saveDescriptionForTeamCircuitRoom(@Param("teamName") String teamName,
                                                         @Param("roomName") String roomName,
                                                         DescriptionInputDto descriptionInputDto);

    @RequestLine("POST " + ResourcePaths.CIRCUIT_PATH + ResourcePaths.TEAM_PATH + "/{teamName}" + ResourcePaths.ROOM_PATH + "/{roomName}" +
            ResourcePaths.PROPERTY_PATH + ResourcePaths.TAGS_PATH)
    TeamCircuitRoomDto saveTagsForTeamCircuitRoom(@Param("teamName") String teamName,
                                                  @Param("roomName") String roomName,
                                                  TagsInputDto tagsInputDto);



}
