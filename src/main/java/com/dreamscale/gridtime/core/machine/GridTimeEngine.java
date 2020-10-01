package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.GridTableResults;
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class GridTimeEngine {

    @Autowired
    private CircuitActivityDashboard circuitActivityDashboard;

    @Autowired
    private GridTimeWorkPile gridTimeWorkPile;

    private GridTimeExecutor gridTimeExecutor;

    @PostConstruct
    public void init() {
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkPile);
    }

    public void start() {
        gridTimeExecutor.start();
    }

    public void reset() {
        gridTimeExecutor.reset();
    }

    public void shutdown() {
        gridTimeExecutor.shutdown();
    }

    public void configureDoneAfterTicks(int ticksToWait) {
        gridTimeExecutor.configureDoneAfterTicks(ticksToWait);
    }

    public void configureDoneAfterTime(long millisToWait) {
        gridTimeExecutor.configureDoneAfterTime(millisToWait);
    }

    public void waitForDone(int timeoutMillis) {
        gridTimeExecutor.waitForDone(timeoutMillis);
    }

    public void waitForDone() {
        gridTimeExecutor.waitForDone();
    }

    public GridTableResults getDashboardStatus(DashboardActivityScope dashboardActivityScope) {
        return circuitActivityDashboard.getDashboardStatus(dashboardActivityScope);
    }


    public void getJobs() {


        //okay, so what if, I made a monitor as part of my main loop
        //that on each tick, printed out the status of each work pile, in some sort of summarized tabular fashion?

        //can I use this table thing, to print arbitrary results in a table?

        //what would my columns be?
    }


    public TorchieCmd getTorchieCmd(UUID torchieId) {
        return gridTimeWorkPile.getTorchieCmd(torchieId);
    }

    public TorchieCmd submitJob(Torchie torchie) {
        return gridTimeWorkPile.submitJob(torchie);
    }



}
