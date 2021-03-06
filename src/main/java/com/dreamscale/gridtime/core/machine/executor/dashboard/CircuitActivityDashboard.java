package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.RefreshDashboardTick;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CircuitActivityDashboard {

    private Map<UUID, DashboardMonitor> dashboardMonitors = DefaultCollections.map();

    private Dashboard dashboard = new Dashboard();

    private int ticksSinceLastRefresh = 0;

    private static final int TICKS_BETWEEN_REFRESH = 20;

    public void addMonitor(MonitorType monitorType, UUID workerId, CircuitMonitor circuitMonitor) {
        addMonitor(monitorType, workerId, circuitMonitor, ProcessStatus.ACTIVE);
    }

    public void addMonitor(MonitorType monitorType, UUID workerId, CircuitMonitor circuitMonitor, ProcessStatus processStatus) {
        DashboardMonitor monitor = new DashboardMonitor(monitorType, workerId, circuitMonitor, processStatus);

        dashboardMonitors.put(workerId, monitor);
    }

    public void updateProcessStatus(UUID workerId, ProcessStatus processStatus) {
        DashboardMonitor monitor = dashboardMonitors.get(workerId);
        if (monitor != null) {
            monitor.setProcessStatus(processStatus);
        } else {
            log.warn("Unable to set process status to "+processStatus + " for worker "+workerId);
        }
    }

    public void evictMonitor(UUID workerId) {
        DashboardMonitor evictedMonitor = dashboardMonitors.remove(workerId);

        if (evictedMonitor != null) {
            dashboard.aggregateFinished(evictedMonitor);
        }
    }


    public boolean checkIfNeedsRefresh(int updatesSinceLastRefresh) {
        ticksSinceLastRefresh++;

        if (ticksSinceLastRefresh > TICKS_BETWEEN_REFRESH && updatesSinceLastRefresh > 0) {
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
            case PLEXER_DETAIL:
                return dashboard.toPlexerTopGridTableResults();
            case TORCHIE_DETAIL:
                return dashboard.toTorchieTopGridTableResults();
            case ALL_DETAIL:
                return dashboard.toTopGridTableResults();
            case FAILURE_DETAIL:
                return dashboard.toFailureDetailResults();
        }
        return null;
    }



    public TickInstructions generateRefreshTick() {
        return new RefreshDashboardTick(this);
    }



    @AllArgsConstructor
    @Getter
    @Setter
    private class DashboardMonitor {
        MonitorType monitorType;
        UUID workerId;
        CircuitMonitor circuitMonitor;
        ProcessStatus processStatus;

    }

    private class Dashboard {

        private CircuitActivitySummaryRow calendarFinished ;
        private CircuitActivitySummaryRow dashboardFinished ;
        private CircuitActivitySummaryRow torchieFinished;
        private CircuitActivitySummaryRow plexerFinished ;

        private CircuitActivitySummaryRow calendarLive ;
        private CircuitActivitySummaryRow dashboardLive ;
        private CircuitActivitySummaryRow torchieLive ;
        private CircuitActivitySummaryRow plexerLive ;

        private Map<UUID, CircuitActivitySummaryRow> finishedProcesses = DefaultCollections.map();

        Dashboard() {
            clear();
        }

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

            CircuitActivitySummaryRow processRow = finishedProcesses.get(dashboardMonitor.getWorkerId());
            if (processRow == null) {
                processRow = createProcessRow(dashboardMonitor);
                finishedProcesses.put(dashboardMonitor.getWorkerId(), processRow);
            }
            processRow.aggregateMonitor(dashboardMonitor.getCircuitMonitor());

        }

        private CircuitActivitySummaryRow createProcessRow(DashboardMonitor dashboardMonitor) {

            String typePrefix = "@sys/";
            if (dashboardMonitor.getMonitorType().equals(MonitorType.PLEXER_WORKER)) {
                typePrefix = "@plexer/";
            } else if (dashboardMonitor.getMonitorType().equals(MonitorType.TORCHIE_WORKER)) {
                typePrefix = "@torchie/";
            } else if (dashboardMonitor.getMonitorType().equals(MonitorType.SYSTEM_CALENDAR)) {
                typePrefix = "@sys/cal/";
            } else if (dashboardMonitor.getMonitorType().equals(MonitorType.SYSTEM_DASHBOARD)) {
                typePrefix = "@sys/dsh/";
            }
            String processId = CellFormat.toCellValue(dashboardMonitor.getWorkerId());
            return new CircuitActivitySummaryRow(dashboardMonitor.getMonitorType(), typePrefix + processId, true);
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
            calendarFinished = new CircuitActivitySummaryRow(MonitorType.SYSTEM_CALENDAR, "@sys/cal", false);
            dashboardFinished = new CircuitActivitySummaryRow(MonitorType.SYSTEM_DASHBOARD, "@sys/dsh", false);
            torchieFinished = new CircuitActivitySummaryRow(MonitorType.TORCHIE_WORKER, "@torchie", false);
            plexerFinished = new CircuitActivitySummaryRow(MonitorType.PLEXER_WORKER, "@plexer", false);

            calendarLive = new CircuitActivitySummaryRow(MonitorType.SYSTEM_CALENDAR, "@sys/cal", false);
            dashboardLive = new CircuitActivitySummaryRow(MonitorType.SYSTEM_DASHBOARD, "@sys/dsh", false);
            torchieLive = new CircuitActivitySummaryRow(MonitorType.TORCHIE_WORKER, "@torchie", false);
            plexerLive = new CircuitActivitySummaryRow(MonitorType.PLEXER_WORKER, "@plexer", false);

            finishedProcesses = DefaultCollections.map();
        }

        public void clearLive() {
            calendarLive = calendarFinished.clone();
            dashboardLive = dashboardFinished.clone();
            torchieLive = torchieFinished.clone();
            plexerLive = plexerFinished.clone();
        }


         public GridTableResults toSummaryGridTableResults() {
             List<List<String>> rowsOfPaddedCells = new ArrayList<>();

             rowsOfPaddedCells.add(calendarLive.toRow());
             rowsOfPaddedCells.add(dashboardLive.toRow());
             rowsOfPaddedCells.add(plexerLive.toRow());
             rowsOfPaddedCells.add(torchieLive.toRow());


             return new GridTableResults("Gridtime Activity Summary", torchieLive.toHeaderRow(), rowsOfPaddedCells);

         }

        public GridTableResults toPlexerTopGridTableResults() {
            Map<UUID, CircuitActivitySummaryRow> rowsInTable = getRowsInTableByType(MonitorType.PLEXER_WORKER);

            return toProcessTopTable("Gridtime Plexer Activity Detail", rowsInTable.values());
        }

        public GridTableResults toTorchieTopGridTableResults() {

            Map<UUID, CircuitActivitySummaryRow> rowsInTable = getRowsInTableByType(MonitorType.TORCHIE_WORKER);

            return toProcessTopTable("Gridtime Torchie Activity Detail", rowsInTable.values());
        }

        public GridTableResults toTopGridTableResults() {

            Map<UUID, CircuitActivitySummaryRow> rowsInTable = getAllRowsInTable();

            return toProcessTopTable("Gridtime Activity Detail", rowsInTable.values());
        }

        public GridTableResults toFailureDetailResults() {

            Map<UUID, CircuitActivitySummaryRow> rowsInTable = getAllFailingRowsInTable();

            return toFailureTable("Gridtime Failure Details", rowsInTable.values());
        }


        private Map<UUID, CircuitActivitySummaryRow> getRowsInTableByType(MonitorType monitorType) {
            List<DashboardMonitor> filteredMonitors = dashboardMonitors.values().stream().filter(
                    (monitor -> monitor.getMonitorType().equals(monitorType)
                    )).collect(Collectors.toList());

            Map<UUID, CircuitActivitySummaryRow> rowsInTable = DefaultCollections.map();

            for (DashboardMonitor monitor : filteredMonitors) {

                CircuitActivitySummaryRow existingRow = finishedProcesses.get(monitor.getWorkerId());
                CircuitActivitySummaryRow newRow = null;

                if (existingRow != null) {
                    newRow = existingRow.clone();
                } else {
                    newRow = createProcessRow(monitor);
                }
                newRow.setProcessStatus(monitor.getProcessStatus());
                newRow.aggregateMonitor(monitor.getCircuitMonitor());
                rowsInTable.put(monitor.getWorkerId(), newRow);
            }

            for (Map.Entry<UUID, CircuitActivitySummaryRow> rowEntry : finishedProcesses.entrySet()) {
                UUID workerId = rowEntry.getKey();
                MonitorType rowType = rowEntry.getValue().getMonitorType();
                CircuitActivitySummaryRow row = rowEntry.getValue();

                if (!rowsInTable.containsKey(workerId) && rowType.equals(monitorType)) {
                    row.setProcessStatus(ProcessStatus.FINISHED);
                    rowsInTable.put(workerId, row);
                }
            }
            return rowsInTable;
        }

        private Map<UUID, CircuitActivitySummaryRow> getAllRowsInTable() {
            Map<UUID, CircuitActivitySummaryRow> rowsInTable = DefaultCollections.map();

            for (DashboardMonitor monitor : dashboardMonitors.values()) {

                CircuitActivitySummaryRow existingRow = finishedProcesses.get(monitor.getWorkerId());
                CircuitActivitySummaryRow newRow = null;

                if (existingRow != null) {
                    newRow = existingRow.clone();
                } else {
                    newRow = createProcessRow(monitor);
                }
                newRow.setProcessStatus(monitor.getProcessStatus());
                newRow.aggregateMonitor(monitor.getCircuitMonitor());
                rowsInTable.put(monitor.getWorkerId(), newRow);
            }

            for (Map.Entry<UUID, CircuitActivitySummaryRow> rowEntry : finishedProcesses.entrySet()) {
                UUID workerId = rowEntry.getKey();
                MonitorType rowType = rowEntry.getValue().getMonitorType();
                CircuitActivitySummaryRow row = rowEntry.getValue();

                if (!rowsInTable.containsKey(workerId)) {
                    row.setProcessStatus(ProcessStatus.FINISHED);
                    rowsInTable.put(workerId, row);
                }
            }
            return rowsInTable;
        }

        private Map<UUID, CircuitActivitySummaryRow> getAllFailingRowsInTable() {
            Map<UUID, CircuitActivitySummaryRow> rowsInTable = getAllRowsInTable();
            Map<UUID, CircuitActivitySummaryRow> failureRows = new LinkedHashMap<>();

            for (Map.Entry<UUID, CircuitActivitySummaryRow> rowEntry : rowsInTable.entrySet()) {
                if (rowEntry.getValue().hasFailure()) {
                   failureRows.put(rowEntry.getKey(), rowEntry.getValue());
                }
            }
            return failureRows;
        }


        private GridTableResults toProcessTopTable(String title, Collection<CircuitActivitySummaryRow> rows) {
            List<List<String>> rowsOfPaddedCells = new ArrayList<>();

            //so this one, we've gotta go back, and create records for each circuit monitor, so I should have another class

            List<CircuitActivitySummaryRow> processRows = createProcessRowsSortedByTop(rows);

            List<String> headers = Collections.emptyList();

            if (processRows.size() > 0) {
                for (CircuitActivitySummaryRow processRow : processRows) {
                    rowsOfPaddedCells.add(processRow.toRow());
                }
                headers = processRows.get(0).toHeaderRow();
            }

            return new GridTableResults(title, headers, rowsOfPaddedCells);
        }

        private GridTableResults toFailureTable(String title, Collection<CircuitActivitySummaryRow> failureRows) {

            List<List<String>> rowsOfPaddedCells = new ArrayList<>();

            List<String> headers = Collections.emptyList();

            for (CircuitActivitySummaryRow row : failureRows) {
                for (Exception ex : row.getLastFailures()) {
                    FailureDetailRow failDetail = new FailureDetailRow(row, ex);

                    rowsOfPaddedCells.addAll(failDetail.toRows());

                    if (headers.isEmpty()) {
                        headers = failDetail.toHeaderRow();
                    }
                }
            }

            return new GridTableResults(title, headers, rowsOfPaddedCells);
        }
    }



        private List<CircuitActivitySummaryRow> createProcessRowsSortedByTop(Collection<CircuitActivitySummaryRow> unsortedRows) {
            List<CircuitActivitySummaryRow> sortedRows = new ArrayList<>(unsortedRows);

            sortedRows.sort((proc1, proc2) -> Integer.compare(proc2.getTicksProcessed(), proc1.getTicksProcessed()));

            return sortedRows;
        }


}
