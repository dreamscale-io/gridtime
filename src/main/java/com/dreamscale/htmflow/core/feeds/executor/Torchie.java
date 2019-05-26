package com.dreamscale.htmflow.core.feeds.executor;

import com.dreamscale.htmflow.api.torchie.TorchieJobStatus;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.Metronome;
import com.dreamscale.htmflow.core.feeds.common.ZoomableFlow;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.feeds.pool.FeatureCache;
import com.dreamscale.htmflow.core.feeds.pool.TileLoader;
import com.dreamscale.htmflow.core.feeds.story.mapper.TileUri;
import com.dreamscale.htmflow.core.feeds.executor.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.feeds.pool.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.FlowSource;
import com.dreamscale.htmflow.core.feeds.executor.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.feeds.executor.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

public class Torchie {

    private final UUID torchieId;

    private final Metronome metronome;
    private final SharedFeaturePool sharedFeaturePool;

    private final ZoomableFlow zoomableFlow;

    private TorchieJobStatus jobStatus;

    public Torchie(UUID torchieId, LocalDateTime startingPosition, TileLoader tileLoader, FeatureCache featureCache) {
        this.torchieId = torchieId;

        this.metronome = new Metronome(startingPosition);
        this.sharedFeaturePool = new SharedFeaturePool(torchieId, metronome.getActiveCoordinates(), tileLoader, featureCache);
        this.jobStatus = new TorchieJobStatus(torchieId, metronome.getActiveCoordinates().formatDreamTime());

        this.zoomableFlow = new ZoomableFlow(torchieId, metronome, sharedFeaturePool);
    }

    public TileBuilder runTile(String tileUri) {
        TileUri.SourceCoordinates coords = TileUri.extractCoordinatesFromUri(tileUri);

        moveFeedPosition(coords.getTileCoordinates().getClockTime());

        tick();
        whatsNext().run();

        return whereAmI();
    }

    private void moveFeedPosition(LocalDateTime middleOfNowhereTime) {
        LocalDateTime tileBeginningTime = GeometryClock.roundDownToNearestTwenty(middleOfNowhereTime);
        metronome.movePosition(tileBeginningTime);

        sharedFeaturePool.movePosition(metronome.getActiveCoordinates());
    }

    void addFlowSourceToChain(FetchStrategy fetchStrategy, FlowObserver... observers) {
        metronome.addFlowToChain(new FlowSource(torchieId, sharedFeaturePool,fetchStrategy, observers));
    }

    void addFlowTransformerToChain(TransformStrategy... transforms) {
        metronome.addFlowToChain(new FlowTransformer(torchieId, sharedFeaturePool, transforms));
    }

    void addFlowSinkToChain(SinkStrategy... sinks) {
        metronome.addFlowToChain(new FlowSink(torchieId, sharedFeaturePool, sinks));
    }

    public UUID getTorchieId() {
        return torchieId;
    }

    /**
     * One tick of progress, generates the next bit of work to do, when there's no more work.
     */
    public void tick() {
        this.zoomableFlow.tick();
    }

    public Runnable whatsNext() {
        return this.zoomableFlow.whatsNext();
    }

    public TileBuilder whereAmI() {
        return this.zoomableFlow.getLastStoryTile();
    }


    public void wrapUpAndBookmark() {
        this.zoomableFlow.wrapUpAndBookmark();
    }

    public TorchieJobStatus getJobStatus() {
        return jobStatus;
    }

    public boolean isDone() {
        //depending on the job type, agents can boot up and die,
        // based on whatever conditions are relevant to the type of job
        return !this.metronome.canTick();
    }



}
