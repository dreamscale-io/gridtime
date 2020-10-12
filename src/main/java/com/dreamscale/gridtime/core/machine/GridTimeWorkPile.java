package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.executor.circuit.ProcessType;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.worker.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class GridTimeWorkPile implements WorkPile {

    @Autowired
    CircuitActivityDashboard circuitActivityDashboard;

    //these are system base jobs, that are prioritized over all other work, like generating calendar
    @Autowired
    private SystemWorkPile systemWorkPile;

    //these are mid-length jobs, that might run for minutes, that generate source tiles requiring aggregation

    @Autowired
    private TorchieWorkPile torchieWorkPile;

    //these are a fixed pool of workers that handle all aggregation related work, triggered off of events

    @Autowired
    private PlexerWorkPile plexerWorkPile;

    public boolean hasWork() {

        systemWorkPile.sync();

        torchieWorkPile.sync();

        return systemWorkPile.hasWork() || torchieWorkPile.hasWork() || plexerWorkPile.hasWork();
    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {

        return torchieWorkPile.getTorchieCmd(torchieId);
    }

    @Override
    public void reset() {
        log.debug("RESET ALL GRIDTIME ENGINE WORK");

        circuitActivityDashboard.clear();

        //these put back the base processes and monitors again after dashboard is clear
        systemWorkPile.reset();
        torchieWorkPile.reset();
        plexerWorkPile.reset();

    }

    @Override
    public void pause() {
        systemWorkPile.pause();
        plexerWorkPile.pause();
        torchieWorkPile.pause();
    }

    @Override
    public void resume() {
        systemWorkPile.resume();
        plexerWorkPile.resume();
        torchieWorkPile.resume();
    }

    public void pauseSystemJobs() {
        systemWorkPile.pause();
    }

    public void resumeSystemJobs() {
        systemWorkPile.resume();
    }

    public void pausePlexerJobs() {
        plexerWorkPile.pause();
    }

    public void resumePlexerJobs() {
        plexerWorkPile.resume();
    }


    public void configureDaysToKeepAhead(int days) {
        systemWorkPile.configureDaysToKeepAhead(days);
    }

    public TickInstructions whatsNext() {

        TickInstructions instructions = null;

        //TODO create a history table, so all evicted processes, write their process stats, above detail row can be persisted

        //TODO query history table and make grid results, for a certain torchie proc.

        //once I get the system calendar job done, write an integration test at the engine level

        //TODO create the ability to kill jobs


        if (circuitActivityDashboard.tickAndCheckIfNeedsRefresh()) {
            systemWorkPile.submitWork(ProcessType.Dashboard, circuitActivityDashboard.generateRefreshTick());
        }

        if (systemWorkPile.hasWork()) {
            instructions = systemWorkPile.whatsNext();
        }

        if (instructions == null && plexerWorkPile.hasWork()) {
            instructions = plexerWorkPile.whatsNext();
        }

        if (instructions == null && torchieWorkPile.hasWork()) {
            instructions = torchieWorkPile.whatsNext();
        }

        return instructions;
    }


    @Override
    public int size() {
        return systemWorkPile.size() + torchieWorkPile.size() + plexerWorkPile.size();
    }

    public TorchieCmd submitJob(Torchie torchie) {
        return torchieWorkPile.submitJob(torchie);
    }

    public void clear() {
        torchieWorkPile.clear();
    }


}




