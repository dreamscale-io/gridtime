package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.GenerateTeamTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregateStreamEvent;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.Program;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.Locas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.LocasFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AggregateWorkerProgram implements Program {

    private final UUID workerId;

    private final Wire inputWire;
    private final LocasFactory locasFactory;

    private AggregateStreamEvent activeEvent;
    private Metronome.Tick activeTick;

    private int latestQueueDepth;

    public AggregateWorkerProgram(UUID workerId, Wire inputWire, LocasFactory locasFactory) {
        this.workerId = workerId;
        this.inputWire = inputWire;
        this.locasFactory = locasFactory;
    }

    @Override
    public void tick() {

        inputWire.markDone(workerId);

        activeEvent = inputWire.pullNext(workerId);
        latestQueueDepth = inputWire.getQueueDepth();


        if (activeEvent != null) {
            activeTick = Metronome.createTick(activeEvent.getGridTime());
        }
    }

    @Override
    public Metronome.Tick getActiveTick() {
        return activeTick;
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        List<TileInstructions> instructions = new ArrayList<>();

        if (activeTick != null) {
            instructions.add(generateAggregateTickInstructions(activeTick));
        }

        return instructions;
    }

    @Override
    public int getInputQueueDepth() {
        return latestQueueDepth;
    }

    private TileInstructions generateAggregateTickInstructions(Metronome.Tick aggregateTick) {

        List<Locas> aggregatorChain = new ArrayList<>();
        aggregatorChain.add(locasFactory.createIdeaFlowTeamAggregatorLocas(activeEvent.getTeamId()));

        return new GenerateTeamTile(aggregatorChain, aggregateTick);
    }


    @Override
    public boolean isDone() {
        return false;
    }


}
