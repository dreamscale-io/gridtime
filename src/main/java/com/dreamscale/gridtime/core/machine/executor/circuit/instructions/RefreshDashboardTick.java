package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.CoordinateResults;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.monitor.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshDashboardTick extends TickInstructions {

    private final CircuitActivityDashboard circuitActivityDashboard;


    public RefreshDashboardTick(CircuitActivityDashboard circuitActivityDashboard ) {
        this.circuitActivityDashboard = circuitActivityDashboard;
    }

    @Override
    public void executeInstruction() throws InterruptedException {

        circuitActivityDashboard.refreshDashboard();
    }

    @Override
    public String getCmdDescription() {
        return "refresh dashboard";
    }
}
