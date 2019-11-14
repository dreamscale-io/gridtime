package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class GridTimeEngine {

    @Autowired
    private GridTimeWorkerPool gridTimeWorkerPool;

    private GridTimeExecutor gridTimeExecutor;

    @PostConstruct
    void init() {
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkerPool);

        gridTimeExecutor.start();

    }

    public TorchieCmd getTorchieCmd(UUID torchieId) {
        return gridTimeWorkerPool.getTorchieCmd(torchieId);
    }

    public TorchieCmd submitJob(Torchie torchie) {
        return gridTimeWorkerPool.submitJob(torchie);
    }

}
