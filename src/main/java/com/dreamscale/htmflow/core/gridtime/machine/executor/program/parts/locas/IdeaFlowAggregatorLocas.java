package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.domain.tile.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input.InputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output.OutputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class IdeaFlowAggregatorLocas extends Locas<ZoomableIdeaFlowMetricsEntity> {


    public IdeaFlowAggregatorLocas(UUID torchieId, FeatureCache featureCache,
                                   InputStrategy<ZoomableIdeaFlowMetricsEntity> input,
                                   OutputStrategy output) {
        super(torchieId, featureCache, input, output);

    }

    @Override
    protected void fillAggregateGrid(AggregateGrid aggregateGrid, List<ZoomableIdeaFlowMetricsEntity> ideaflowInputs) {

        for (ZoomableIdeaFlowMetricsEntity ideaflow : ideaflowInputs) {

            RelativeBeat beat = aggregateGrid.getBeat(ideaflow.getGridTime());
            Duration durationWeight = Duration.ofSeconds(ideaflow.getTimeInTile());

            aggregateGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_WTF, beat, durationWeight, ideaflow.getPercentWtf());
            aggregateGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_LEARNING, beat, durationWeight, ideaflow.getPercentLearning());
            aggregateGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_PROGRESS, beat, durationWeight, ideaflow.getPercentProgress());
            aggregateGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_PAIRING, beat, durationWeight, ideaflow.getPercentPairing());
            aggregateGrid.addWeightedMetric(MetricRowKey.ZOOM_AVG_FLAME, beat, durationWeight, ideaflow.getAvgFlame());

            aggregateGrid.addTimeInTile(beat, durationWeight);

        }

    }


}
