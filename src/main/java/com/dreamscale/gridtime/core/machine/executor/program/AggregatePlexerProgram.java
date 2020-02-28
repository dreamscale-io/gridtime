package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateTeamTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.LocasFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AggregatePlexerProgram implements Program {

    private final UUID workerId;

    private final Wire inputWire;
    private final LocasFactory locasFactory;
    private final FeatureCacheManager featureCacheManager;

    private AggregateStreamEvent activeEvent;
    private Metronome.TickScope activeTick;

    private int latestQueueDepth;

    public AggregatePlexerProgram(UUID workerId, Wire inputWire, LocasFactory locasFactory, FeatureCacheManager featureCacheManager) {
        this.workerId = workerId;
        this.inputWire = inputWire;
        this.locasFactory = locasFactory;
        this.featureCacheManager = featureCacheManager;
    }


    @Override
    public String getName() {
        return "AggregatePlexer";
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
    public Metronome.TickScope getActiveTick() {
        return activeTick;
    }

    @Override
    public List<TickInstructions> getInstructionsAtActiveTick() {
        List<TickInstructions> instructions = new ArrayList<>();

        if (activeTick != null) {
            instructions.add(generateAggregateTickInstructions(activeTick));
        }

        return instructions;
    }

    @Override
    public int getInputQueueDepth() {
        return latestQueueDepth;
    }

    private TickInstructions generateAggregateTickInstructions(Metronome.TickScope aggregateTick) {

        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(activeEvent.getTeamId());

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
