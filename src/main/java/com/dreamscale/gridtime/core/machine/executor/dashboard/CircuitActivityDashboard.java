package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.RefreshDashboardTick;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CircuitActivityDashboard {

    private Map<UUID, CircuitMonitor> systemMonitors = DefaultCollections.map();
    private Map<UUID, CircuitMonitor> plexerMonitors = DefaultCollections.map();
    private Map<UUID, CircuitMonitor> torchieMonitors = DefaultCollections.map();

    private Dashboard dashboard = new Dashboard();

    private int ticksSinceLastRefresh = 0;

    private static final int TICKS_BETWEEN_REFRESH = 20;

    public void addMonitor(MonitorType monitorType, UUID workerId, CircuitMonitor circuitMonitor) {
        getCircuitMonitorMap(monitorType).put(workerId, circuitMonitor);
    }

    public void evictMonitor(MonitorType monitorType, UUID workerId) {
        CircuitMonitor evictedMonitor = getCircuitMonitorMap(monitorType).remove(workerId);

        dashboard.updateEvicted(monitorType, evictedMonitor);
    }


    public boolean tickAndCheckIfNeedsRefresh() {
        ticksSinceLastRefresh++;

        if (ticksSinceLastRefresh > TICKS_BETWEEN_REFRESH) {

            ticksSinceLastRefresh = 0;

            return true;
        }

        return false;
    }

    private Map<UUID, CircuitMonitor> getCircuitMonitorMap(MonitorType monitorType) {
        if (monitorType == MonitorType.SYS_WORKER) {
            return systemMonitors;
        } else if (monitorType == MonitorType.PLEXER_WORKER) {
            return plexerMonitors;
        } else if (monitorType == MonitorType.TORCHIE_WORKER) {
            return torchieMonitors;
        }
        return systemMonitors;
    }

    public void clear() {
        systemMonitors.clear();
        plexerMonitors.clear();
        torchieMonitors.clear();

        refresh();
    }

    public void refresh() {

        dashboard.update(MonitorType.SYS_WORKER, createSummary(MonitorType.SYS_WORKER));
        dashboard.update(MonitorType.PLEXER_WORKER, createSummary(MonitorType.PLEXER_WORKER));
        dashboard.update(MonitorType.TORCHIE_WORKER, createSummary(MonitorType.TORCHIE_WORKER));
    }


    public GridTableResults getDashboardStatus(DashboardActivityScope dashboardActivityScope) {
        switch (dashboardActivityScope) {
            case GRID_SUMMARY:
                return dashboard.toSummaryGridTableResults();
            case SYSTEM_DETAIL:
                return dashboard.toSystemTopGridTableResults();
            case PLEXER_DETAIL:
                return dashboard.toPlexerTopGridTableResults();
            case TORCHIE_DETAIL:
                return dashboard.toTorchieTopGridTableResults();
            default:
        }
        return null;
    }


    private CircuitActivitySummaryRow createSummary(MonitorType monitorType) {

        Map<UUID, CircuitMonitor> monitorMap = getCircuitMonitorMap(monitorType);

        CircuitActivitySummaryRow summary = new CircuitActivitySummaryRow();

        for (CircuitMonitor monitor : monitorMap.values()) {

            summary.aggregateMonitor(monitor);
        }

        return summary;
    }

    public TickInstructions generateRefreshTick() {
        return new RefreshDashboardTick(this);
    }


    private class Dashboard {
        private CircuitActivitySummaryRow evictedSystemActivity = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow evictedTorchieActivity = new CircuitActivitySummaryRow();

        private CircuitActivitySummaryRow activeSystemActivity = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow activePlexerActivity = new CircuitActivitySummaryRow();
        private CircuitActivitySummaryRow activeTorchieActivity = new CircuitActivitySummaryRow();

        public void updateEvicted(MonitorType monitorType, CircuitMonitor evictedMonitor) {
            if (monitorType == MonitorType.TORCHIE_WORKER) {
                evictedTorchieActivity.aggregateMonitor(evictedMonitor);
            } else if (monitorType == MonitorType.SYS_WORKER) {
                evictedSystemActivity.aggregateMonitor(evictedMonitor);
            }
        }

        public void update(MonitorType monitorType, CircuitActivitySummaryRow activeSummary) {

            if (monitorType == MonitorType.TORCHIE_WORKER) {
                activeTorchieActivity = activeSummary;
            } else if (monitorType == MonitorType.PLEXER_WORKER) {
                activePlexerActivity = activeSummary;
            } else if (monitorType == MonitorType.SYS_WORKER) {
                activeSystemActivity = activeSummary;
            }
         }

         public GridTableResults toSummaryGridTableResults() {
             List<List<String>> rowsOfPaddedCells = new ArrayList<>();

             rowsOfPaddedCells.add(activeSystemActivity.toRow("@proc/sys.now"));
             rowsOfPaddedCells.add(evictedSystemActivity.toRow("@proc/sys.done"));

             rowsOfPaddedCells.add(activePlexerActivity.toRow("@proc/plexer.now"));

             rowsOfPaddedCells.add(activeTorchieActivity.toRow("@proc/torchie.now"));
             rowsOfPaddedCells.add(evictedTorchieActivity.toRow("@proc/torchie.done"));

             return new GridTableResults("Gridtime Activity Summary", activeTorchieActivity.toHeaderRow(), rowsOfPaddedCells);

         }

        public GridTableResults toPlexerTopGridTableResults() {
            return toProcessTopTable("Gridtime Plexer Activity", plexerMonitors.values());
        }

        public GridTableResults toSystemTopGridTableResults() {
            return toProcessTopTable("Gridtime System Activity", systemMonitors.values());
        }

        public GridTableResults toTorchieTopGridTableResults() {
            return toProcessTopTable("Gridtime Torchie Activity", torchieMonitors.values());
        }

        private GridTableResults toProcessTopTable(String title, Collection<CircuitMonitor> monitors) {
            List<List<String>> rowsOfPaddedCells = new ArrayList<>();

            //so this one, we've gotta go back, and create records for each circuit monitor, so I should have another class

            List<ProcessDetailsRow> processRows = createProcessRowsSortedByTop(monitors);

            List<String> headers = Collections.emptyList();

            if (processRows.size() > 0) {
                for (ProcessDetailsRow processRow : processRows) {
                    rowsOfPaddedCells.add(processRow.toRow("@proc/"+processRow.getProcessId()));
                }
                headers = processRows.get(0).toHeaderRow();
            }

            return new GridTableResults(title, headers, rowsOfPaddedCells);
        }

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
