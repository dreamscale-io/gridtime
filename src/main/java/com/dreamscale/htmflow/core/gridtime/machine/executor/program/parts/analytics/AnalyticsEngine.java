package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.IMusicGrid;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.FeatureMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;

import java.util.List;

public class AnalyticsEngine {

    private final FeatureCache featureCache;
    private final MusicClock musicClock;
    private final IMusicGrid musicGrid;

    IdeaFlowMetrics ideaFlowMetrics;
    List<FeatureMetrics> featureMetrics;

    public AnalyticsEngine(FeatureCache featureCache, MusicClock musicClock, IMusicGrid musicGrid) {
        this.featureCache = featureCache;
        this.musicClock = musicClock;
        this.musicGrid = musicGrid;
    }

    public void runIdeaFlowMetrics() {
        ideaFlowMetrics = IdeaFlowMetrics.queryFrom(musicClock, musicGrid);
    }




    public void runFeatureMetrics() {
        featureMetrics = FeatureMetrics.queryFrom(featureCache, musicClock, musicGrid);
    }

    public IdeaFlowMetrics getIdeaFlowMetrics() {
        return ideaFlowMetrics;
    }

    public List<FeatureMetrics> getFeatureMetrics() {
        return featureMetrics;
    }
}
