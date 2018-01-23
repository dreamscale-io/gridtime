package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.ProjectDto;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JournalClient {

    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.PROJECT_PATH)
    List<ProjectDto> getProjects();

    /*
    @RequestLine("POST /somepath")
    void createThingie(Thingie thingie);

    @RequestLine("GET /otherpath/{pathParam}?queryarg={someArg}")
    Thingie getThingieWithQueyrArg(@Param("pathParam") String pathParam, @Param("someArg") String queryArg);
    */
}
