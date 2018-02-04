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
public interface JournalClient {

    @RequestLine("POST " + ResourcePaths.JOURNAL_PATH + ResourcePaths.EVENT_PATH + ResourcePaths.CHUNK_PATH)
    ChunkEventOutputDto createChunkEvent(ChunkEventInputDto chunkEvent);



    /*
    @RequestLine("POST /somepath")
    void createThingie(Thingie thingie);

    @RequestLine("GET /otherpath/{pathParam}?queryarg={someArg}")
    Thingie getThingieWithQueyrArg(@Param("pathParam") String pathParam, @Param("someArg") String queryArg);
    */
}
