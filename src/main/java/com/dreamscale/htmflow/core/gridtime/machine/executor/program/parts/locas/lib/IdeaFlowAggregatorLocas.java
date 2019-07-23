package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.lib;

import com.dreamscale.htmflow.core.domain.tile.GridIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.Locas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.in.BreatheInStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.out.BreatheOutStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IdeaFlowAggregatorLocas implements Locas {

    private final BreatheInStrategy<IdeaFlowMetrics> in;
    private final BreatheOutStrategy out;

    private final UUID torchieId;
    private final FeatureCache featureCache;
    private AggregateGrid locasGrid;

    public IdeaFlowAggregatorLocas(UUID torchieId, FeatureCache featureCache, BreatheInStrategy<IdeaFlowMetrics> in, BreatheOutStrategy out) {
        this.torchieId = torchieId;
        this.featureCache = featureCache;
        this.in = in;
        this.out = out;

    }

    @Override
    public void breatheIn(Metronome.Tick tick) {
        List<IdeaFlowMetrics> metrics = in.breatheIn(torchieId, tick);

        log.debug("metrics: "+metrics.size());

        this.locasGrid = generateGrid(tick.getFrom(), metrics);



        //now I need to process, and aggregate this... do I make a spreadsheet?
        //how can I do a simplified spreadsheet, to process all these metrics?

        //featureCache, gridTime, MusicClock
    }


    @Override
    public void breatheOut(Metronome.Tick tick) {
        out.breatheOut(torchieId, tick, locasGrid);
    }
    private AggregateGrid generateGrid(GeometryClock.GridTime gridTime, List<IdeaFlowMetrics> allEntities) {
        AggregateGrid aggregateGrid = new AggregateGrid(featureCache, gridTime, new MusicClock(gridTime.getZoomLevel()));

        for (IdeaFlowMetrics entity : allEntities) {

//            IdeaFlowMetrics metrics = toIdeaFlowMetrics(entity);
//            aggregateGrid.addMetricColumn( entity.getTileSeq(), metrics.toProps());
        }


        return aggregateGrid;
    }




}
