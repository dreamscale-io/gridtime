package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.TwilightCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.WorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;


@Component
public class AggregationWorkerPool implements WorkerPool {

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    WorkToDoQueueWire workToDoWire;

    @Autowired
    FeatureCacheManager featureCacheManager;


    private static final int DEFAULT_NUMBER_AGGREGATE_WORKERS = 5;

    private int currentPoolSize;
    private WhatsNextWheel<TileInstructions> whatsNextWheel;

    @PostConstruct
    public void init() {
        this.currentPoolSize = DEFAULT_NUMBER_AGGREGATE_WORKERS;
        this.whatsNextWheel = createWhatsNextWheel(currentPoolSize);
    }

    private WhatsNextWheel<TileInstructions> createWhatsNextWheel(int initialPoolSize) {

        WhatsNextWheel whatsNextWheel = new WhatsNextWheel<TileInstructions>();

        for (int i = 0; i < initialPoolSize; i++) {
            UUID workerId = UUID.randomUUID();
            CircuitMonitor circuitMonitor = new CircuitMonitor(workerId);
            TwilightCircuit circuit = new TwilightCircuit(circuitMonitor, programFactory.createAggregateWorkerProgram(workerId, featureCacheManager));

            whatsNextWheel.addWorker(workerId, circuit);
        }

        return whatsNextWheel;
    }

    public boolean hasWork() {
        //TODO need to check the DB, wired in here, should call the actual DB,
        //but then the work items, saving the events, should mark when all are ready



        //so if I've got all the work items for the team, then I can grab it
        //if I have some of the work items, then process after a delay

        //group by count on the queue table, checks the actual work...

        //then if I try to actually get the work, and there is no work,

        return workToDoWire.getQueueDepth() > 0;
    }

    public TileInstructions whatsNext() {
       return whatsNextWheel.whatsNext();
    }

    @Override
    public void evictLastWorker() {
        //no-op, workers can't be evicted for now
    }

    @Override
    public int size() {
        return whatsNextWheel.size();
    }
}
