package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.TwilightCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;

import java.util.UUID;

public class AggregationWorkerPool implements WorkerPool {


    private final FeatureCacheManager featureCacheManager;
    private final ProgramFactory programFactory;
    private final Wire workToDoWire;

    private static final int DEFAULT_NUMBER_AGGREGATE_WORKERS = 5;

    private int currentPoolSize;
    private WhatsNextWheel<TileInstructions> whatsNextWheel;

    public AggregationWorkerPool(ProgramFactory programFactory, Wire workToDoWire, FeatureCacheManager featureCacheManager) {
        this.programFactory = programFactory;
        this.workToDoWire = workToDoWire;
        this.featureCacheManager = featureCacheManager;

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
    public void addWorker(Worker worker) {
        //no-op, workers can't be added for now
    }

    @Override
    public boolean containsWorker(UUID workerId) {
        return whatsNextWheel.contains(workerId);
    }

    @Override
    public Worker getWorker(UUID workerId) {
        return whatsNextWheel.getWorker(workerId);
    }


    @Override
    public void evictLastWorker() {
        //no-op, workers can't be evicted for now
    }
}
