package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatus;
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.executor.dashboard.DashboardActivityScope;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class GridTimeEngine {

    @Autowired
    private GridClock gridClock;

    @Autowired
    private CircuitActivityDashboard circuitActivityDashboard;

    @Autowired
    private FeedDataManager feedDataManager;

    @Autowired
    private GridTimeWorkPile gridTimeWorkPile;

    private GridTimeExecutor gridTimeExecutor;


    @PostConstruct
    public void init() {
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkPile);
    }

    public SimpleStatusDto start() {
        return gridTimeExecutor.start();
    }

    public SimpleStatusDto restart() {
        return gridTimeExecutor.restart();
    }

    public void reset() {
        gridTimeExecutor.reset();
    }

    public SimpleStatusDto shutdown() {
        return gridTimeExecutor.shutdown();
    }

    public void configureDoneAfterTicks(int ticksToWait) {
        gridTimeExecutor.configureDoneAfterTicks(ticksToWait);
    }

    public void configureDoneAfterTime(long millisToWait) {
        gridTimeExecutor.configureDoneAfterTime(millisToWait);
    }

    public void configureDaysToKeepAhead(int days) {
        gridTimeWorkPile.configureDaysToKeepAhead(days);
    }

    public void waitForDone(int timeoutMillis) {
        gridTimeExecutor.waitForDone(timeoutMillis);
    }

    public void waitForDone() {
        gridTimeExecutor.waitForDone();
    }

    public GridTableResults getDashboard(DashboardActivityScope dashboardActivityScope) {
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

    public GridStatusSummaryDto getStatusSummary() {
        GridStatusSummaryDto summaryDto = new GridStatusSummaryDto();

        GridStatus status = gridTimeExecutor.getStatus();
        summaryDto.setGridStatus(status);
        summaryDto.setMessage(status.getMessage());

        String lastError = gridTimeExecutor.getLastError();

        if (!gridTimeExecutor.isRunning() && lastError != null) {
            summaryDto.setMessage("Last Error: "+ lastError);
        }

        GridTableResults processSummary = circuitActivityDashboard.getDashboardStatus(DashboardActivityScope.GRID_SUMMARY);
        summaryDto.setActivitySummary(processSummary);

        return summaryDto;
    }

    public SimpleStatusDto purgeAll() {
        shutdown();

        feedDataManager.purgeAll();

        reset();
        return new SimpleStatusDto(Status.SUCCESS, "Gridtime shutdown and all feed data purged.");
    }
}
