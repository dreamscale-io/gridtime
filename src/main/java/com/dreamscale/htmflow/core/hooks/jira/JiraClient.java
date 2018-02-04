package com.dreamscale.htmflow.core.hooks.jira;


import com.dreamscale.htmflow.api.ResourcePaths;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JiraClient {

    @RequestLine("GET " + JiraPaths.API_PATH + ResourcePaths.PROJECT_PATH)
    List<JiraProjectDto> getProjects();

}
