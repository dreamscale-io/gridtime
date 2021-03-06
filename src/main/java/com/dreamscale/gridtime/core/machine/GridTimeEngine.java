package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.grid.GridStatus;
import com.dreamscale.gridtime.api.grid.GridStatusSummaryDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.SystemCmd;
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

    @Autowired
    private GridTimeExecutor gridTimeExecutor;

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

    public void setAutorun(boolean isAutorun) {
        gridTimeWorkPile.setAutorun(isAutorun);
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
        if (gridTimeExecutor.isRunning()) {
            return gridTimeWorkPile.getTorchieCmd(torchieId);
        }
        else {
            throw new RuntimeException("Gridtime engine isn't running, please start before retrieving TorchieCmd");
        }
    }

    public void releaseTorchieCmd(TorchieCmd cmd) {
        gridTimeWorkPile.releaseTorchieCmd(cmd);
    }

    public SystemCmd getSystemCmd() {
        if (gridTimeExecutor.isRunning()) {
            return gridTimeWorkPile.getSystemCmd();
        }
        else {
            throw new RuntimeException("Gridtime engine isn't running, please start before retrieving SystemCmd");
        }
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
        return new SimpleStatusDto(Status.SUCCESS, "Gridtime shutdown and all feed and calendar data purged.");
    }

    public SimpleStatusDto purgeFeeds() {
        shutdown();

        feedDataManager.purgeFeeds();

        reset();
        return new SimpleStatusDto(Status.SUCCESS, "Gridtime shutdown and all feed data purged.");
    }



}
