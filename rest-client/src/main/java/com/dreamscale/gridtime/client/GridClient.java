package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface GridClient {

    @RequestLine("POST " + ResourcePaths.GRID_PATH + ResourcePaths.START_PATH)
    SimpleStatusDto start();

    @RequestLine("POST " + ResourcePaths.GRID_PATH + ResourcePaths.SHUTDOWN_PATH)
    SimpleStatusDto shutdown();

    @RequestLine("POST " + ResourcePaths.GRID_PATH + ResourcePaths.RESTART_PATH)
    SimpleStatusDto restart();

    @RequestLine("GET " + ResourcePaths.GRID_PATH + ResourcePaths.STATUS_PATH)
    GridStatusSummaryDto getStatus();

    @RequestLine("GET " + ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH)
    GridTableResults getTopProcesses();

    @RequestLine("GET " + ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH + ResourcePaths.FAIL_PATH)
    GridTableResults getProcessFailureDetails();

    @RequestLine("GET " + ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH + ResourcePaths.TORCHIE_PATH)
    GridTableResults getTopTorchieProcesses();

    @RequestLine("GET " + ResourcePaths.GRID_PATH + ResourcePaths.PROCESS_PATH + ResourcePaths.PLEXER_PATH)
    GridTableResults getTopPlexerProcesses();

}

