package com.dreamscale.htmflow.core.feeds.executor.parts.loops;


import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.clock.Metronome;
import com.dreamscale.htmflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.SinkStrategy;

import java.util.ArrayList;
import java.util.List;

public class StrangeLoop {

    private List<Flow> flowsToPlay;
    private List<LoopStrategy> loops;
    private List<SinkStrategy> sinkStrategies;

    private final Metronome metronome;
    private final SharedFeaturePool sharedFeaturePool;

    public StrangeLoop(Metronome metronome, SharedFeaturePool sharedFeaturePool) {
        this.metronome = metronome;
        this.sharedFeaturePool = sharedFeaturePool;
        this.flowsToPlay = new ArrayList<>();
        this.loops = new ArrayList<>();
        this.sinkStrategies = new ArrayList<>();
    }

    public void playFeed(Flow flow) {
        this.flowsToPlay.add(flow);
    }

    public void sink(SinkStrategy sinkStrategy) {
        this.sinkStrategies.add(sinkStrategy);
    }

    public void searchSimilar(LoopStrategy loopStrategy) {
        this.loops.add(loopStrategy);
    }

    public void splitDifferences(LoopStrategy loopStrategy) {
        this.loops.add(loopStrategy);
    }


//    public void flow(ExecutorService executorPool, IntensityLevel intensity) {
//
//    }



}
