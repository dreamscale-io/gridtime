package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableBoxTimeLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableTimeLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.AggregateMetricGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.BoxAggregateMetricGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class ZoomableBoxLocas extends ZoomableBoxTimeLocas<ZoomableBoxMetricsEntity> {


    public ZoomableBoxLocas(UUID teamId, UUID torchieId, FeatureCache featureCache,
                            InputStrategy<ZoomableBoxMetricsEntity> input,
                            OutputStrategy output) {
        super(teamId, torchieId, featureCache, input, output);

    }

    @Override
    protected void fillAggregateGrid(BoxAggregateMetricGrid aggregateMetricGrid, FeatureCache featureCache, List<ZoomableBoxMetricsEntity> boxInputMetrics) {

        for (ZoomableBoxMetricsEntity boxMetrics : boxInputMetrics) {

            PlaceReference boxReference = featureCache.lookupBoxReferenceWithUri(boxMetrics.getBoxFeatureId(), boxMetrics.getBoxUri());

            RelativeBeat beat = aggregateMetricGrid.getBeat(boxMetrics.getGridTime());
            Duration durationWeight = Duration.ofSeconds(boxMetrics.getTimeInBox());

            //need box as group here...
            aggregateMetricGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_WTF, beat, durationWeight, boxMetrics.getPercentWtf());
            aggregateMetricGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_LEARNING, beat, durationWeight, boxMetrics.getPercentLearning());
            aggregateMetricGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_PROGRESS, beat, durationWeight, boxMetrics.getPercentProgress());
            aggregateMetricGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_PAIRING, beat, durationWeight, boxMetrics.getPercentPairing());
            aggregateMetricGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_AVG_FLAME, beat, durationWeight, boxMetrics.getAvgFlame());

        }

    }




}
