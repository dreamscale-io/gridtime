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

    /*
    @RequestLine("POST /somepath")
    void createThingie(Thingie thingie);

    @RequestLine("GET /otherpath/{pathParam}?queryarg={someArg}")
    Thingie getThingieWithQueyrArg(@Param("pathParam") String pathParam, @Param("someArg") String queryArg);
    */
}
