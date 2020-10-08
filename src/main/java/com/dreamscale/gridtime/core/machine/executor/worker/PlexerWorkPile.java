package com.dreamscale.gridtime.core.machine.executor.worker;

import com.dreamscale.gridtime.core.machine.executor.circuit.ProcessType;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.dashboard.CircuitActivityDashboard;
import com.dreamscale.gridtime.core.machine.executor.dashboard.MonitorType;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;


@Component
public class PlexerWorkPile implements WorkPile {

    @Autowired
    private CircuitActivityDashboard circuitActivityDashboard;

    @Autowired
    ProgramFactory programFactory;

    @Autowired
    AggregateWorkToDoQueueWire workToDoWire;

    @Autowired
    FeatureCacheManager featureCacheManager;


    private static final int DEFAULT_NUMBER_PLEXER_WORKERS = 5;

    private int currentPoolSize;
    private WhatsNextWheel<TickInstructions> whatsNextWheel;
    private boolean paused = false;

    @PostConstruct
    public void init() {
        this.currentPoolSize = DEFAULT_NUMBER_PLEXER_WORKERS;
        this.whatsNextWheel = createWhatsNextWheel(currentPoolSize);
    }

    private WhatsNextWheel<TickInstructions> createWhatsNextWheel(int initialPoolSize) {

        WhatsNextWheel whatsNextWheel = new WhatsNextWheel<TickInstructions>();

        for (int i = 0; i < initialPoolSize; i++) {
            UUID workerId = UUID.randomUUID();
            CircuitMonitor circuitMonitor = new CircuitMonitor(ProcessType.Plexer, workerId);
            IdeaFlowCircuit circuit = new IdeaFlowCircuit(circuitMonitor, programFactory.createAggregatePlexerProgram(workerId, featureCacheManager));

            circuitActivityDashboard.addMonitor(MonitorType.PLEXER_WORKER, workerId, circuitMonitor);

            whatsNextWheel.addWorker(workerId, circuit);
        }

        return whatsNextWheel;
    }

    public boolean hasWork() {
        return workToDoWire.getQueueDepth() > 0;
    }

    @Override
    public void reset() {
        whatsNextWheel.clear();
        whatsNextWheel = createWhatsNextWheel(currentPoolSize);

        paused = false;
    }

    @Override
    public void pause() {
        paused = true;
    }

    @Override
    public void resume() {
        paused = false;
    }


    public TickInstructions whatsNext() {
        if (paused) return null;

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
