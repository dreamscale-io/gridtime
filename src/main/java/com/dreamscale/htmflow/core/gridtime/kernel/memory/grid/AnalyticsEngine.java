package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.IdeaFlowMetrics;

public class AnalyticsEngine {

    private final FeatureCache featureCache;
    private final MusicClock musicClock;
    private final MusicGrid musicGrid;

    IdeaFlowMetrics ideaFlowMetrics;

    public AnalyticsEngine(FeatureCache featureCache, MusicClock musicClock, MusicGrid musicGrid) {
        this.featureCache = featureCache;
        this.musicClock = musicClock;
        this.musicGrid = musicGrid;
    }

    public IdeaFlowMetrics runIdeaFlowMetrics() {
        ideaFlowMetrics = IdeaFlowMetrics.queryFrom(featureCache, musicClock, musicGrid);

        return ideaFlowMetrics;
    }

    public IdeaFlowMetrics getIdeaFlowMetrics() {
        return ideaFlowMetrics;
    }






}
