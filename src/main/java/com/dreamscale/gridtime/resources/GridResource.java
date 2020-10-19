package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto;
import com.dreamscale.gridtime.core.machine.GridTimeEngine;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.GRID_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class GridResource {

    @Autowired
    GridTimeEngine gridTimeEngine;

    /**
     * Startup the gridtime engine, and start processing data
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.START_PATH)
    public SimpleStatusDto start() {
        return gridTimeEngine.start();
    }

    /**
     * Shutdown the gridtime engine, and stop processing data
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SHUTDOWN_PATH)
    public SimpleStatusDto shutdown() {
        return gridTimeEngine.shutdown();
    }

    /**
     * Restart the gridtime engine, shutting down all threads and reinitializing the system
     *
     * @return SimpleStatusDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.RESTART_PATH)
    public SimpleStatusDto restart() {
        return gridTimeEngine.restart();
    }

    /**
     * Get the status of the gridtime engine, running, shutdown, restarting, etc
     *
     * @return GridTableResults
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.STATUS_PATH)
    public GridStatusSummaryDto getStatus() {
        return gridTimeEngine.getStatusSummary();
    }

    /**
     * Get the latest process detail for all processes
     *
     * @return GridTableResults
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PROCESS_PATH )
    public GridTableResults getTopProcesses() {
        return gridTimeEngine.getDashboard(DashboardActivityScope.ALL_DETAIL);
    }

    /**
     * Get the latest process detail for all torchie processes
     *
     * @return GridTableResults
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PROCESS_PATH + ResourcePaths.TORCHIE_PATH)
    public GridTableResults getTopTorchieProcesses() {
        return gridTimeEngine.getDashboard(DashboardActivityScope.TORCHIE_DETAIL);
    }

    /**
     * Get the latest process detail for all plexer processes
     *
     * @return GridTableResults
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PROCESS_PATH + ResourcePaths.PLEXER_PATH)
    public GridTableResults getTopPlexerProcesses() {
        return gridTimeEngine.getDashboard(DashboardActivityScope.PLEXER_DETAIL);
    }
}

