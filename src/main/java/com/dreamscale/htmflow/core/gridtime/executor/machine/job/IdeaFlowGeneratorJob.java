package com.dreamscale.htmflow.core.gridtime.executor.machine.job;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.GenerateAggregateUpTile;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.GenerateNextTile;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.FlowSource;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;

import java.util.List;
import java.util.UUID;

public class IdeaFlowGeneratorJob implements MetronomeJob {

    private final UUID torchieId;

    private final FeaturePool featurePool;
    private final List<Flow> pullChain = DefaultCollections.list();

    public IdeaFlowGeneratorJob(UUID torchieId, FeaturePool featurePool) {
        this.torchieId = torchieId;
        this.featurePool = featurePool;
    }

    public void addFlowSourceToPullChain(FetchStrategy fetchStrategy, FlowObserver... observers) {
        addFlowToPullChain(new FlowSource(torchieId, featurePool,fetchStrategy, observers));
    }

    public void addFlowTransformerToPullChain(TransformStrategy... transforms) {
        addFlowToPullChain(new FlowTransformer(torchieId, featurePool, transforms));
    }

    public void addFlowSinkToPullChain(SinkStrategy... sinks) {
        addFlowToPullChain(new FlowSink(torchieId, featurePool, sinks));
    }

    private void addFlowToPullChain(Flow flow) {
        this.pullChain.add(flow);
    }

    @Override
    public void gotoPosition(GeometryClock.GridTime gridTime) {
        featurePool.gotoPosition(gridTime);
    }

    @Override
    public TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        return new GenerateNextTile(featurePool, pullChain, fromGridTime, toGridTime);
    }

    @Override
    public TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        return new GenerateAggregateUpTile(featurePool, fromGridTime, toGridTime);
    }
}
