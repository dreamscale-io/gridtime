package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.RefreshDashboardTick;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;

@Component
public class CircuitActivityDashboard {

    private Map<UUID, DashboardMonitor> dashboardMonitors = DefaultCollections.map();

    private Dashboard dashboard = new Dashboard();

    private int ticksSinceLastRefresh = 0;

    private static final int TICKS_BETWEEN_REFRESH = 20;

    public void addMonitor(MonitorType monitorType, UUID workerId, CircuitMonitor circuitMonitor) {
        DashboardMonitor monitor = new DashboardMonitor(monitorType, workerId, circuitMonitor);

        dashboardMonitors.put(workerId, monitor);
    }


    public void evictMonitor(UUID workerId) {
        DashboardMonitor evictedMonitor = dashboardMonitors.remove(workerId);

        if (evictedMonitor != null) {
            dashboard.aggregateFinished(evictedMonitor);
        }
    }



    public boolean tickAndCheckIfNeedsRefresh() {
        ticksSinceLastRefresh++;

        if (ticksSinceLastRefresh > TICKS_BETWEEN_REFRESH) {

            ticksSinceLastRefresh = 0;

            return true;
        }

        return false;
    }



    public void clear() {
        dashboardMonitors.clear();
        dashboard.clear();
    }

    public void refresh() {

        dashboard.clearLive();

        for (DashboardMonitor monitor : dashboardMonitors.values()) {
            dashboard.aggregateLive(monitor);
        }
    }


    public GridTableResults getDashboardStatus(DashboardActivityScope dashboardActivityScope) {
        refresh();

        switch (dashboardActivityScope) {
            case GRID_SUMMARY:
                return dashboard.toSummaryGridTableResults();
//            case SYSTEM_DETAIL:
//                return dashboard.toSystemTopGridTableResults();
//            case PLEXER_DETAIL:
//                return dashboard.toPlexerTopGridTableResults();
//            case TORCHIE_DETAIL:
//                return dashboard.toTorchieTopGridTableResults();
//            default:
        }
        return null;
    }



    public TickInstructions generateRefreshTick() {
        return new RefreshDashboardTick(this);
    }

    @AllArgsConstructor
    @Getter
    private class DashboardMonitor {
        MonitorType monitorType;
        UUID workerId;
        CircuitMonitor circuitMonitor;
    }

    private class Dashboard {

        private CircuitActivitySummaryRow calendarFinished = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow dashboardFinished = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow torchieFinished = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow plexerFinished = new CircuitActivitySummaryRow();

        private CircuitActivitySummaryRow calendarLive = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow dashboardLive = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow torchieLive = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow plexerLive = new CircuitActivitySummaryRow();


        public void aggregateFinished(DashboardMonitor dashboardMonitor) {

            switch (dashboardMonitor.getMonitorType()) {
                case TORCHIE_WORKER:
                    torchieFinished.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case PLEXER_WORKER:
                    plexerFinished.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case SYSTEM_CALENDAR:
                    calendarFinished.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case SYSTEM_DASHBOARD:
                    dashboardFinished.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
            }

        }

        public void aggregateLive(DashboardMonitor dashboardMonitor) {

            switch (dashboardMonitor.getMonitorType()) {
                case TORCHIE_WORKER:
                    torchieLive.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case PLEXER_WORKER:
                    plexerLive.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case SYSTEM_CALENDAR:
                    calendarLive.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
                    break;
                case SYSTEM_DASHBOARD:
                    dashboardLive.aggregateMonitor(dashboardMonitor.getCircuitMonitor());
            }

        }

        public void clear() {
            calendarFinished = new CircuitActivitySummaryRow();
            dashboardFinished = new CircuitActivitySummaryRow();
            torchieFinished = new CircuitActivitySummaryRow();
            plexerFinished = new CircuitActivitySummaryRow();

            calendarLive = new CircuitActivitySummaryRow();
            dashboardLive = new CircuitActivitySummaryRow();
            torchieLive = new CircuitActivitySummaryRow();
            plexerLive = new CircuitActivitySummaryRow();
        }

        public void clearLive() {
            calendarLive = calendarFinished.clone();
            dashboardLive = dashboardFinished.clone();
            torchieLive = torchieFinished.clone();
            plexerLive = plexerFinished.clone();
        }
//
//
         public GridTableResults toSummaryGridTableResults() {
             List<List<String>> rowsOfPaddedCells = new ArrayList<>();

             rowsOfPaddedCells.add(calendarLive.toRow("@sys/cal"));
             rowsOfPaddedCells.add(dashboardLive.toRow("@sys/dash"));

             rowsOfPaddedCells.add(plexerLive.toRow("@plexer"));

             rowsOfPaddedCells.add(torchieLive.toRow("@torchie"));

             return new GridTableResults("Gridtime Activity Summary", torchieLive.toHeaderRow(), rowsOfPaddedCells);

         }
//
//        public GridTableResults toPlexerTopGridTableResults() {
//            return toProcessTopTable("Gridtime Plexer Activity", plexerMonitors.values());
//        }
//
//        public GridTableResults toSystemTopGridTableResults() {
//            return toProcessTopTable("Gridtime System Activity", systemMonitors.values());
//        }
//
//        public GridTableResults toTorchieTopGridTableResults() {
//            return toProcessTopTable("Gridtime Torchie Activity", torchieMonitors.values());
//        }
//
//        private GridTableResults toProcessTopTable(String title, Collection<CircuitMonitor> monitors) {
//            List<List<String>> rowsOfPaddedCells = new ArrayList<>();
//
//            //so this one, we've gotta go back, and create records for each circuit monitor, so I should have another class
//
//            List<ProcessDetailsRow> processRows = createProcessRowsSortedByTop(monitors);
//
//            List<String> headers = Collections.emptyList();
//
//            if (processRows.size() > 0) {
//                for (ProcessDetailsRow processRow : processRows) {
//                    rowsOfPaddedCells.add(processRow.toRow("@proc/"+processRow.getProcessId()));
//                }
//                headers = processRows.get(0).toHeaderRow();
//            }
//
//            return new GridTableResults(title, headers, rowsOfPaddedCells);
//        }

        private List<ProcessDetailsRow> createProcessRowsSortedByTop(Collection<CircuitMonitor> monitors) {
            List<ProcessDetailsRow> processRows = new ArrayList<>();

            for (CircuitMonitor monitor: monitors) {
                processRows.add(new ProcessDetailsRow(monitor));
            }

            processRows.sort((proc1, proc2) -> Integer.compare(proc2.getTicksProcessed(), proc1.getTicksProcessed()));

            return processRows;
        }

    }
}
