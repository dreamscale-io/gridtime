package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableTimeLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.grid.AggregateMetricGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ZoomableIdeaFlowLocas extends ZoomableTimeLocas<ZoomableIdeaFlowMetricsEntity> {


    public ZoomableIdeaFlowLocas(UUID teamId, UUID torchieId, FeatureCache featureCache,
                                 InputStrategy<ZoomableIdeaFlowMetricsEntity> input,
                                 OutputStrategy output) {
        super(teamId, torchieId, featureCache, input, output);

    }

    @Override
    protected void fillAggregateGrid(AggregateMetricGrid aggregateMetricGrid, List<ZoomableIdeaFlowMetricsEntity> ideaflowInputs) {

        for (ZoomableIdeaFlowMetricsEntity ideaflow : ideaflowInputs) {

            RelativeBeat beat = aggregateMetricGrid.getBeat(ideaflow.getGridTime());
            Duration durationWeight = Duration.ofSeconds(ideaflow.getTimeInTile());

            aggregateMetricGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_WTF, beat, durationWeight, ideaflow.getPercentWtf());
            aggregateMetricGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_LEARNING, beat, durationWeight, ideaflow.getPercentLearning());
            aggregateMetricGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_PROGRESS, beat, durationWeight, ideaflow.getPercentProgress());
            aggregateMetricGrid.addWeightedMetric(MetricRowKey.ZOOM_PERCENT_PAIRING, beat, durationWeight, ideaflow.getPercentPairing());
            aggregateMetricGrid.addWeightedMetric(MetricRowKey.ZOOM_AVG_FLAME, beat, durationWeight, ideaflow.getAvgFlame());

            aggregateMetricGrid.addTimeInTile(beat, durationWeight);

        }

    }


}
