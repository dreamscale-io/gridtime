package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateTeamTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.LocasFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AggregatePlexerProgram implements Program {

    private final UUID workerId;

    private final Wire inputWire;
    private final LocasFactory locasFactory;

    private AggregateStreamEvent activeEvent;
    private Metronome.TickScope activeTick;

    private int latestQueueDepth;

    public AggregatePlexerProgram(UUID workerId, Wire inputWire, LocasFactory locasFactory) {
        this.workerId = workerId;
        this.inputWire = inputWire;
        this.locasFactory = locasFactory;
    }


    @Override
    public String getName() {
        return "AggregatePlexer";
    }

    @Override
    public void tick() {

        //TODO this done call is weird, seems like it should be on a finishing work thing, not here.
        inputWire.markDone(workerId);

        activeEvent = inputWire.pullNext(workerId);
        latestQueueDepth = inputWire.getQueueDepth();


        if (activeEvent != null) {
            activeTick = Metronome.createTick(activeEvent.getGridTime());
        }
    }

    @Override
    public Metronome.TickScope getActiveTick() {
        return activeTick;
    }

    @Override
    public List<TickInstructions> getInstructionsAtActiveTick() {
        List<TickInstructions> instructions = new ArrayList<>();

        if (activeEvent != null && activeTick != null) {
            instructions.add(generateAggregateTickInstructions(activeEvent, activeTick));
        }

        return instructions;
    }

    @Override
    public int getInputQueueDepth() {
        return latestQueueDepth;
    }

    private TickInstructions generateAggregateTickInstructions(AggregateStreamEvent activeEvent, Metronome.TickScope aggregateTick) {

        List<Locas> aggregatorChain = new ArrayList<>();
        aggregatorChain.add(locasFactory.createIdeaFlowTeamAggregatorLocas(activeEvent.getTeamId()));
        aggregatorChain.add(locasFactory.createTeamBoxAggregatorLocas(activeEvent.getTeamId()));

        return new GenerateTeamTile(aggregatorChain, aggregateTick);
    }


    @Override
    public boolean isDone() {
        return false;
    }


}
