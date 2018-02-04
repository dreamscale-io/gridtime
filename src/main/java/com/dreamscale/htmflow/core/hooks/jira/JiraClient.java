package com.dreamscale.htmflow.core.hooks.jira;


import com.dreamscale.htmflow.api.ResourcePaths;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JiraClient {

    @Headers({
            "Authorization: {user}:{apiKey}"
    })
    @RequestLine("GET " + JiraPaths.API_PATH + ResourcePaths.PROJECT_PATH)
    List<JiraProjectDto> getProjects(@Param("user") String user, @Param("apiKey") String apiKey);

}
