package org.dreamscale.htmflow.client;

import feign.Headers;
import feign.RequestLine;
import org.dreamscale.htmflow.api.ResourcePaths;
import org.dreamscale.htmflow.api.context.ProjectDto;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface ContextClient {

    @RequestLine("GET " + ResourcePaths.PROJECT_PATH)
    List<ProjectDto> getProjects();

}
