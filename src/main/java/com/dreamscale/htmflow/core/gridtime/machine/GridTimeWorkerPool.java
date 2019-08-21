package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregatingWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.ProgramFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.AggregationWorkerPool;
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.TorchieWorkerPool;
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.Worker;
import com.dreamscale.htmflow.core.gridtime.machine.executor.workpile.WorkerPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class GridTimeWorkerPool implements WorkerPool {

    private AggregationWorkerPool aggregateWorkerPool;

    private TorchieWorkerPool torchieWorkerPool = new TorchieWorkerPool();

    boolean lastInstructionIsTorchie = false;

    public GridTimeWorkerPool(ProgramFactory programFactory, Wire workToDoWire) {
        this.aggregateWorkerPool = new AggregationWorkerPool(programFactory, workToDoWire);
    }

    public boolean hasWork() {
        return torchieWorkerPool.hasWork() || aggregateWorkerPool.hasWork();
    }

    public TileInstructions whatsNext() {

        TileInstructions instructions = null;

        if (aggregateWorkerPool.hasWork()) {
            instructions = aggregateWorkerPool.whatsNext();
            if (instructions != null) {
                lastInstructionIsTorchie = false;
            }
        }

        if (instructions == null) {
            instructions = torchieWorkerPool.whatsNext();
            lastInstructionIsTorchie = true;
        }

        return instructions;
    }


    @Override
    public Worker getWorker(UUID workerId) {
        return torchieWorkerPool.getWorker(workerId);
    }

    public void startTorchieIfNotActiveInPile(Torchie torchie) {
        if (!torchieWorkerPool.containsWorker(torchie)) {
            torchieWorkerPool.addWorker(torchie);
        }
    }

    @Override
    public void addWorker(Worker torchie) {
        torchieWorkerPool.addWorker(torchie);
    }

    @Override
    public boolean containsWorker(UUID torchieId) {
        return torchieWorkerPool.containsWorker(torchieId);
    }

    public Torchie getTorchie(UUID torchieId) {
        return torchieWorkerPool.getWorker(torchieId);
    }

    public void evictLastWorker() {
        if (lastInstructionIsTorchie) {
            torchieWorkerPool.evictLastWorker();
        }
    }


    //Ive got Torchie source feeds

    //And I've got worker programs that are stateless, and all operate off the same queue

    //but there is a queue that builds up, and I need a circuit monitor, for the workers pool

    //each worker gets it's own id, circuit monitor for each worker thread.


    //I've got Torchies that are for source feeds,
    //generating work to do, in a pool, round robin work to do

    //then I need to create a work to do table

    //and then I get a batch of work to do, periodically, and a number of team workers,
    // that respond to the work to do, and generate dynamic program, for different sorts of work
    // that needs doing

    //writes out status table, when it grabs rows for processing
    //consumption queue program
    //effects the self-balancing around the generation of more work to do, with handling the events
    //dont generate more work, while the queue of work_to_do is full

    //Wire - write event to work to do...



    //so if I've got a list of teams, each team gets 1 throughput unit per member, 1 click per team size

    //so if I've got a big team, and a small team, running on the same server

//    public TileInstructions pullNext() {
//
//    }

//    private TileInstructions getNextSupplyChain() {
////        int i = 0;
////
////        while (i < nextTeamQueue.size()) {
////            UUID teamId = nextTeamQueue.get(i);
////            if (isTeamReady(teamId)) {
////                nextTeamQueue.remove(i);
////                nextTeamQueue.add(teamId);
////                return sourceFeedTorchies.get(torchieId).whatsNext();
////            }
////        }
////
////        return null;
//    }

    private class TeamSupplyChain {
        private final UUID teamId;
        private final AggregatingWire teamWire;
        private Torchie teamTorchie;

        private final LinkedList<UUID> whatsNextQueue = new LinkedList<>();
        private Map<UUID, Torchie> sourceFeedTorchies = new HashMap<>();

        int currentPassCounter;
        int teamSize;

        public TeamSupplyChain(UUID teamId) {
            this.teamId = teamId;
            this.teamWire = new AggregatingWire(teamId);
        }


        private boolean isTorchieReady(UUID torchieId) {
            Torchie torchie = sourceFeedTorchies.get(torchieId);
            return torchie.getCircuitMonitor().isReady();
        }


    }


}




