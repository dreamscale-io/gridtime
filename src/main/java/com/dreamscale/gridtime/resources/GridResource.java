package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto;
import com.dreamscale.gridtime.api.terminal.Command;
import com.dreamscale.gridtime.api.terminal.ActivityContext;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRoute;
import com.dreamscale.gridtime.core.capability.terminal.TerminalRouteRegistry;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.machine.GridTimeEngine;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.GRID_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class GridResource {

    @Autowired
    GridTimeEngine gridTimeEngine;

    @Autowired
    private TerminalRouteRegistry terminalRouteRegistry;

    @PostConstruct
    void init() {
        terminalRouteRegistry.register(ActivityContext.SYSTEM,
                Command.GRID, "Start and stop the Gridtime operating system.", new GridTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.SYSTEM,
                Command.PS, "Show top process activity.", new PsTerminalRoute());

        terminalRouteRegistry.register(ActivityContext.SYSTEM,
                Command.ERROR, "Show error details.", new ErrorTerminalRoute());


    }

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
     * Get the latest failure details for all processes
     *
     * @return GridTableResults
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.PROCESS_PATH + ResourcePaths.ERROR_PATH)
    public GridTableResults getProcessFailureDetails() {
        return gridTimeEngine.getDashboard(DashboardActivityScope.FAILURE_DETAIL);
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


    private class GridTerminalRoute extends TerminalRoute {

        private static final String OPERATION_PARAM = "operation";

        private static final String START_OPERATION_CHOICE = "start";
        private static final String STOP_OPERATION_CHOICE = "stop";
        private static final String RESTART_OPERATION_CHOICE = "restart";
        private static final String STATUS_OPERATION_CHOICE = "status";

        GridTerminalRoute() {
            super(Command.GRID, "{" + OPERATION_PARAM + "}");

            describeTextOption(OPERATION_PARAM, "operation to send to the grid");
            describeChoiceOption(OPERATION_PARAM, START_OPERATION_CHOICE, STOP_OPERATION_CHOICE, RESTART_OPERATION_CHOICE, STATUS_OPERATION_CHOICE);
        }

        @Override
        public Object route(Map<String, String> params) {
            String operation = params.get(OPERATION_PARAM);

            if (operation.equals(START_OPERATION_CHOICE) ) {
                return start();
            }

            if (operation.equals(STOP_OPERATION_CHOICE)) {
                return shutdown();
            }

            if (operation.equals(RESTART_OPERATION_CHOICE)) {
                return restart();
            }

            if (operation.equals(STATUS_OPERATION_CHOICE)) {
                return getStatus();
            }

            throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find a matching terminal command to execute");

        }
    }


    private class PsTerminalRoute extends TerminalRoute {

        private static final String SCOPE_PARAM = "scope";
        private static final String ALL_PS_CHOICE = "all";
        private static final String TORCHIE_PS_CHOICE = "torchie";
        private static final String PLEXER_PS_CHOICE = "plexer";

        PsTerminalRoute() {
            super(Command.PS, "{" + SCOPE_PARAM + "}");

            describeChoiceOption(SCOPE_PARAM, ALL_PS_CHOICE, TORCHIE_PS_CHOICE, PLEXER_PS_CHOICE);
        }

        @Override
        public Object route(Map<String, String> params) {
            String scope = params.get(SCOPE_PARAM);

            if (scope.equals(ALL_PS_CHOICE) ) {
                return getTopProcesses();
            }

            if (scope.equals(TORCHIE_PS_CHOICE) ) {
                return getTopTorchieProcesses();
            }

            if (scope.equals(PLEXER_PS_CHOICE) ) {
                return getTopPlexerProcesses();
            }

            throw new BadRequestException(ValidationErrorCodes.UNABLE_TO_FIND_TERMINAL_ROUTE, "Unable to find a matching terminal command to execute");

        }

    }

    private class ErrorTerminalRoute extends TerminalRoute {

        ErrorTerminalRoute() {
            super(Command.ERROR, "");
        }

        @Override
        public Object route(Map<String, String> params) {

            return getProcessFailureDetails();
        }

    }
}

