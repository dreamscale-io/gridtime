package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.executor.circuit.wires.WorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class GridTimeEngine {

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    WorkToDoQueueWire workToDoQueueWire;

    @Autowired
    TorchieFactory torchieFactory;

    @Autowired
    FeatureCacheManager featureCacheManager;

    private GridTimeWorkerPool gridTimeWorkerPool;

    private GridTimeExecutor gridTimeExecutor;


    @PostConstruct
    void init() {
        this.gridTimeWorkerPool = new GridTimeWorkerPool(programFactory, workToDoQueueWire, featureCacheManager);
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkerPool);
    }


}
