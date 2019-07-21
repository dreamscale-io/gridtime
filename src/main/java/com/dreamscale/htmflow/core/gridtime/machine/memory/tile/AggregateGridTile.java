package com.dreamscale.htmflow.core.gridtime.machine.memory.tile;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateMusicGrid;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.AnalyticsEngine;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.FeatureMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Slf4j
public class AggregateGridTile {

    //aggregates I've got use case of horizontal rollup, as is the case of 20s to day parts
    //I've also got use case of vertical rollup, as is the case of dayparts rolled up to team day parts

    //so different sources can load, and I might have an unlimited number of tiles rolling into a team.
    //could have a super giant big team that outstrips memory and requires piece-wise processing.

    //with the feature aggregates, there's a process of getting the featureFeed, and processing so many at a time


    private final UUID torchieId;
    private final GeometryClock.GridTime gridTime;
    private final FeatureCache featureCache;

    private final ZoomLevel zoomLevel;
    private final MusicClock musicClock;

    private final AggregateMusicGrid musicGrid;
    private final AnalyticsEngine analyticsEngine;

    public AggregateGridTile(UUID torchieId, GeometryClock.GridTime gridTime, FeatureCache featureCache) {
        this.torchieId = torchieId;
        this.gridTime = gridTime;
        this.featureCache = featureCache;

        this.zoomLevel = gridTime.getZoomLevel();
        this.musicClock = new MusicClock(zoomLevel);

        this.musicGrid = new AggregateMusicGrid(featureCache, gridTime, musicClock);
        this.analyticsEngine = new AnalyticsEngine(featureCache, musicClock, musicGrid);

    }

    public void loadTileMetrics(LocalDateTime moment, IdeaFlowMetrics metrics) {
        musicGrid.loadTileMetrics(moment, metrics);
    }

    public void loadFeatureMetrics(FeatureReference featureReference, FeatureMetrics featureMetrics) {
        musicGrid.loadFeatureMetrics(featureReference, featureMetrics);
    }

    public void finishAfterLoad() {
        musicGrid.finish();

        analyticsEngine.runIdeaFlowMetrics();
        analyticsEngine.runFeatureMetrics();
    }

}
