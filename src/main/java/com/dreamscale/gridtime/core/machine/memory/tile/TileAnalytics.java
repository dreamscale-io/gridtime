package com.dreamscale.gridtime.core.machine.memory.tile;

import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.MusicGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;

import java.util.List;

public class TileAnalytics {

    private final FeatureCache featureCache;
    private final MusicClock musicClock;
    private final MusicGrid musicGrid;

    private IdeaFlowMetrics ideaFlowMetrics;
    private List<BoxMetrics> boxMetrics;

    public TileAnalytics(FeatureCache featureCache, MusicClock musicClock, MusicGrid musicGrid) {
        this.featureCache = featureCache;
        this.musicClock = musicClock;
        this.musicGrid = musicGrid;
    }

    public void runMetrics() {
        ideaFlowMetrics = IdeaFlowMetrics.queryFrom(musicClock, musicGrid);

        //landscape metrics
        boxMetrics = BoxMetrics.queryFrom(musicGrid);

    }

    public IdeaFlowMetrics getIdeaFlowMetrics() {
        return ideaFlowMetrics;
    }

    public List<BoxMetrics> getBoxMetrics() {
        return boxMetrics;
    }

}
