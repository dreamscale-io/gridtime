package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.account.UsernameInputDto;
import com.dreamscale.gridtime.api.project.*;
import com.dreamscale.gridtime.api.team.TeamInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface ProjectClient {

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH)
    List<ProjectDto> getProjects();

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.SEARCH_PATH + "/{startsWith}")
    List<TaskDto> findTasksStartingWith(@Param("id") String projectId, @Param("startsWith") String startsWith);

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH + "/{id}" )
    ProjectDetailsDto getProjectDetails(@Param("id") String projectId);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.BOX_PATH)
    SimpleStatusDto updateBoxConfiguration(@Param("id") String projectId , ProjectBoxConfigurationInputDto boxConfigurationInput);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.GRANT_PATH + ResourcePaths.USERNAME_PATH)
    SimpleStatusDto grantPermissionToUser(@Param("id") String projectId , UsernameInputDto usernameInputDto);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.REVOKE_PATH + ResourcePaths.USERNAME_PATH)
    SimpleStatusDto revokePermissionFromUser(@Param("id") String projectId , UsernameInputDto usernameInputDto);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.GRANT_PATH + ResourcePaths.TEAM_PATH)
    SimpleStatusDto grantPermissionToTeam(@Param("id") String projectId , TeamInputDto teamInputDto);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.CONFIG_PATH + ResourcePaths.REVOKE_PATH + ResourcePaths.TEAM_PATH)
    SimpleStatusDto revokePermissionFromTeam(@Param("id") String projectId , TeamInputDto teamInputDto);


}
