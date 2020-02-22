package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.capability.operator.GridtimeJobManager;
import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class GridTimeEngine {

    @Autowired
    private GridtimeJobManager gridtimeJobManager;

    @Autowired
    private GridTimeWorkPile gridTimeWorkerPool;

    private GridTimeExecutor gridTimeExecutor;

    @PostConstruct
    void init() {
        this.start();
    }

    public void start() {
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkerPool);

        gridTimeExecutor.start();
    }

    public void destroy() {
        if (this.gridTimeExecutor != null) {
            this.gridTimeExecutor.shutdown();
        }
    }

    public void getJobs() {

    }


    public TorchieCmd getTorchieCmd(UUID torchieId) {
        return gridTimeWorkerPool.getTorchieCmd(torchieId);
    }

    public TorchieCmd submitJob(Torchie torchie) {
        return gridTimeWorkerPool.submitJob(torchie);
    }

}
