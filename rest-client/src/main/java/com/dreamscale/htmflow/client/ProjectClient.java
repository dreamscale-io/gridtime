package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.ChunkEventInputDto;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
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

}
