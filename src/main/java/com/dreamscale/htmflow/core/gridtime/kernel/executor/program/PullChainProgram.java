package com.dreamscale.htmflow.core.gridtime.kernel.executor.program;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.GenerateAggregateUpTile;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.GenerateNextTile;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.source.FlowSource;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PullChainProgram implements MetronomeProgram {

    private final UUID torchieId;

    private final FeaturePool featurePool;
    private final List<Flow> pullChain = DefaultCollections.list();
    private final LocalDateTime startPosition;

    public PullChainProgram(UUID torchieId, FeaturePool featurePool, LocalDateTime startPosition) {
        this.torchieId = torchieId;
        this.featurePool = featurePool;
        this.startPosition = startPosition;
    }

    @Override
    public LocalDateTime getStartPosition() {
        return startPosition;
    }

    @Override
    public boolean canTick(LocalDateTime nextPosition) {
        return nextPosition.isBefore(LocalDateTime.now());
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

    public void addFlowSourceToPullChain(FetchStrategy fetchStrategy, FlowObserver... observers) {
        pullChain.add(new FlowSource(torchieId, featurePool,fetchStrategy, observers));
    }

    public void addFlowTransformerToPullChain(TransformStrategy... transforms) {
        pullChain.add(new FlowTransformer(torchieId, featurePool, transforms));
    }

    public void addFlowSinkToPullChain(SinkStrategy... sinks) {
        pullChain.add(new FlowSink(torchieId, featurePool, sinks));
    }
}
