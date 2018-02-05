package com.dreamscale.htmflow.core.hooks.jira;


import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.journal.ChunkEventOutputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface JiraClient {

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.PROJECT_PATH)
    List<JiraProjectDto> getProjects();

    @RequestLine("GET " + JiraPaths.API_PATH + JiraPaths.SEARCH_PATH +
            "?jql={query}&fields={fields}")
    JiraSearchResultPage getTasks(@Param("query") String query, @Param("fields") String fields);
//
//    @RequestLine("GET " + ResourcePaths.JOURNAL_PATH + ResourcePaths.CHUNK_PATH
//            + "?from_date={fromDate}&to_date={toDate}")
//    List<ChunkEventOutputDto> getChunks(@Param("fromDate") String fromDate, @Param("toDate") String toDate);
//
//
//    curl -v 'https://dreamscale.atlassian.net/rest/api/2/search?jql=project%3D10000&fields=id,key,summary' --user janelle@dreamscale.io:9KC0iM24tfXf8iKDVP2q4198 | jq
//

    //+and+status+not+in+(Done)+order+by+updated+desc&fields=id,key,summary

//    {
//        "expand": "operations,versionedRepresentations,editmeta,changelog,renderedFields",
//            "id": "10003",
//            "self": "https://dreamscale.atlassian.net/rest/api/2/issue/10003",
//            "key": "TOR-4",
//            "fields": {
//        "summary": "Create Front-End Services for Journal"
//    }
//    },

}
