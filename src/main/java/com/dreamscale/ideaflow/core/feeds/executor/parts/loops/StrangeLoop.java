package com.dreamscale.ideaflow.core.feeds.executor.parts.loops;


import com.dreamscale.ideaflow.core.feeds.common.IdeaFlow;
import com.dreamscale.ideaflow.core.feeds.clock.Metronome;
import com.dreamscale.ideaflow.core.feeds.common.SharedFeaturePool;
import com.dreamscale.ideaflow.core.feeds.executor.parts.sink.SinkStrategy;

import java.util.ArrayList;
import java.util.List;

public class StrangeLoop {

    private List<IdeaFlow> flowsToPlay;
    private List<LoopStrategy> loops;
    private List<SinkStrategy> sinks;

    private final Metronome metronome;
    private final SharedFeaturePool sharedFeaturePool;

    public StrangeLoop(Metronome metronome, SharedFeaturePool sharedFeaturePool) {
        this.metronome = metronome;
        this.sharedFeaturePool = sharedFeaturePool;
        this.flowsToPlay = new ArrayList<>();
        this.loops = new ArrayList<>();
        this.sinks = new ArrayList<>();
    }

    public void playFeed(IdeaFlow ideaFlow) {
        this.flowsToPlay.add(ideaFlow);
    }

    public void sink(SinkStrategy sinkStrategy) {
        this.sinks.add(sinkStrategy);
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
