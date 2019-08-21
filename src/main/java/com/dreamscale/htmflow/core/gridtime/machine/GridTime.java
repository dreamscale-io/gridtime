package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.WorkToDoQueueWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.ProgramFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class GridTime {

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    WorkToDoQueueWire workToDoQueueWire;

    @Autowired
    TorchieFactory torchieFactory;

    private GridTimeWorkerPool gridTimeWorkerPool;

    private GridTimeExecutor gridTimeExecutor;

    @PostConstruct
    void init() {
        this.gridTimeWorkerPool = new GridTimeWorkerPool(programFactory, workToDoQueueWire);
        this.gridTimeExecutor = new GridTimeExecutor(gridTimeWorkerPool);
    }


}
