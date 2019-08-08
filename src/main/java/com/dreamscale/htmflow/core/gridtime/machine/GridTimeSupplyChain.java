package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregatingWire;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class GridTimeSupplyChain {


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

    private Map<UUID, TeamSupplyChain> teamSupplyChains = DefaultCollections.map();

    private final LinkedList<UUID> nextTeamQueue = new LinkedList<>();

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

    public void addTorchieSource(UUID teamId, Torchie torchie) {
        TeamSupplyChain teamSupplyChain = findOrCreateTeamSupplyChain(teamId);
        teamSupplyChain.wireUpTorchieSource(torchie);
    }

    public void addTeamTorchie(UUID teamId, Torchie torchie) {
        TeamSupplyChain teamSupplyChain = findOrCreateTeamSupplyChain(teamId);
        teamSupplyChain.wireUpTeamTorchie(torchie);
    }

    private TeamSupplyChain findOrCreateTeamSupplyChain(UUID teamId) {
        TeamSupplyChain teamSupplyChain = teamSupplyChains.get(teamId);
        if (teamSupplyChain == null) {
            teamSupplyChain = new TeamSupplyChain(teamId);
            teamSupplyChains.put(teamId, teamSupplyChain);
        }
        return teamSupplyChain;
    }


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

        public void wireUpTorchieSource(Torchie torchie) {
            sourceFeedTorchies.put(torchie.getTorchieId(), torchie);

            torchie.configureOutputStreamEventWire(teamWire);

            currentPassCounter = 0;
            teamSize = sourceFeedTorchies.size();
        }

        public void wireUpTeamTorchie(Torchie torchie) {
            teamTorchie = torchie;
            teamTorchie.configureInputStreamEventWire(teamWire);

            currentPassCounter = 0;
        }

        public TileInstructions pullNext() {

            if (currentPassCounter == 0 ) {

                if (teamTorchie != null) {
                    CircuitMonitor teamCircuit = teamTorchie.getCircuitMonitor();

                    if (teamCircuit.getQueueDepth() > 0) {
                        if (teamCircuit.isReady()) {
                            return teamTorchie.whatsNext();
                        } else {
                            return getWhatsNextForTeamMembers();
                        }
                    } else {
                        currentPassCounter = teamSize;
                    }
                } else {
                    currentPassCounter = teamSize;
                }
            }

            currentPassCounter--;
            return getWhatsNextForTeamMembers();
        }


        private TileInstructions getWhatsNextForTeamMembers() {
            int i = 0;

            while (i < whatsNextQueue.size()) {
                UUID torchieId = whatsNextQueue.get(i);
                if (isTorchieReady(torchieId)) {
                    whatsNextQueue.remove(i);
                    whatsNextQueue.add(torchieId);
                    return sourceFeedTorchies.get(torchieId).whatsNext();
                }
            }

            return null;
        }

        private boolean isTorchieReady(UUID torchieId) {
            Torchie torchie = sourceFeedTorchies.get(torchieId);
            return torchie.getCircuitMonitor().isReady();
        }


    }



}




