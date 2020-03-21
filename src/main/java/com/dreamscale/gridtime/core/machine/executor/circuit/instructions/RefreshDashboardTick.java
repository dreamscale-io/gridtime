package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RefreshDashboardTick extends TickInstructions {

    private final CircuitActivityDashboard circuitActivityDashboard;


    public RefreshDashboardTick(CircuitActivityDashboard circuitActivityDashboard ) {
        this.circuitActivityDashboard = circuitActivityDashboard;
    }

    @Override
    public void executeInstruction() throws InterruptedException {

        circuitActivityDashboard.refresh();
    }

    @Override
    public String getCmdDescription() {
        return "refresh dashboard";
    }
}
