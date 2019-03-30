package com.dreamscale.ideaflow.client;

import com.dreamscale.ideaflow.api.ResourcePaths;
import com.dreamscale.ideaflow.api.project.*;
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

    @RequestLine("POST " + ResourcePaths.PROJECT_PATH + "/{id}" + ResourcePaths.TASK_PATH)
    TaskDto createNewTask(@Param("id") String projectId, TaskInputDto taskInputDto);

}
