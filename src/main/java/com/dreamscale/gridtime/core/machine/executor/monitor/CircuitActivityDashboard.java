package com.dreamscale.gridtime.core.machine.executor.monitor;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.RefreshDashboardTick;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        if (monitorType == MonitorType.SYSTEM_WORKER) {
            return systemMonitors;
        } else if (monitorType == MonitorType.PLEXER_WORKER) {
            return plexerMonitors;
        } else if (monitorType == MonitorType.TORCHIE_WORKER) {
            return torchieMonitors;
        }
        return systemMonitors;
    }

    public void refreshDashboard() {

        dashboard.update(MonitorType.SYSTEM_WORKER, createSummary(MonitorType.SYSTEM_WORKER));
        dashboard.update(MonitorType.PLEXER_WORKER, createSummary(MonitorType.PLEXER_WORKER));
        dashboard.update(MonitorType.TORCHIE_WORKER, createSummary(MonitorType.TORCHIE_WORKER));
    }

    private CircuitActivitySummary createSummary(MonitorType monitorType) {

        Map<UUID, CircuitMonitor> monitorMap = getCircuitMonitorMap(monitorType);

        CircuitActivitySummary summary = new CircuitActivitySummary();

        for (CircuitMonitor monitor : monitorMap.values()) {

            summary.aggregateMonitor(monitor);
        }

        return summary;
    }

    public TickInstructions generateRefreshTick() {
        return new RefreshDashboardTick(this);
    }


    private class Dashboard {
        CircuitActivitySummary evictedSystemActivity = new CircuitActivitySummary();
        CircuitActivitySummary evictedTorchieActivity = new CircuitActivitySummary();

        CircuitActivitySummary activeSystemActivity = new CircuitActivitySummary();
        CircuitActivitySummary activePlexerActivity = new CircuitActivitySummary();
        CircuitActivitySummary activeTorchieActivity = new CircuitActivitySummary();

        void updateEvicted(MonitorType monitorType, CircuitMonitor evictedMonitor) {
            if (monitorType == MonitorType.TORCHIE_WORKER) {
                evictedTorchieActivity.aggregateMonitor(evictedMonitor);
            } else if (monitorType == MonitorType.SYSTEM_WORKER) {
                evictedSystemActivity.aggregateMonitor(evictedMonitor);
            }
        }

        void update(MonitorType monitorType, CircuitActivitySummary activeSummary) {

            if (monitorType == MonitorType.TORCHIE_WORKER) {
                activeTorchieActivity = activeSummary;
            } else if (monitorType == MonitorType.PLEXER_WORKER) {
                activePlexerActivity = activeSummary;
            } else if (monitorType == MonitorType.SYSTEM_WORKER) {
                activeSystemActivity = activeSummary;
            }
         }

         MusicGridResults toRows() {
             List<List<String>> rowsOfPaddedCells = new ArrayList<>();

             rowsOfPaddedCells.add(activeSystemActivity.toRow("@proc/system.now"));
             rowsOfPaddedCells.add(evictedSystemActivity.toRow("@proc/system.done"));

             rowsOfPaddedCells.add(activePlexerActivity.toRow("@proc/plexer"));

             rowsOfPaddedCells.add(activeTorchieActivity.toRow("@proc/torchie.now"));
             rowsOfPaddedCells.add(evictedTorchieActivity.toRow("@proc/torchie.done"));

             return new MusicGridResults("Circuit Dashboard", activeTorchieActivity.toHeaderRow(), rowsOfPaddedCells);

         }

    }
}
