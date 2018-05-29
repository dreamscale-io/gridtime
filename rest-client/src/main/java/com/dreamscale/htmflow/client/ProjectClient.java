package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface ProjectClient {

//    @Headers("Authorization: Bearer {apiKey}")
//    @RequestLine("GET /jira/story/{storyId}")
//    String getStory(@Param("apiKey") String apiKey, @Param("storyId") String storyId);

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH)
    List<ProjectDto> getProjects();

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.TASK_PATH)
    List<TaskDto> getOpenTasksForProject(@Param("id") String projectId);

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH + ResourcePaths.TASK_PATH + ResourcePaths.RECENT_PATH)
    RecentTasksByProjectDto getRecentTasksByProject();

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.TASK_PATH + ResourcePaths.NAME_PATH + "/{name}")
    TaskDto findTaskByName(@Param("id") String projectId, @Param("name") String taskName);

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.TASK_PATH)
    TaskDto createNewTask(@Param("id") String projectId, TaskInputDto taskInputDto);

}
