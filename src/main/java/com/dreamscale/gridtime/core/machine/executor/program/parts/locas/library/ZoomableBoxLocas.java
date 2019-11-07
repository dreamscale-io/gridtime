package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableBoxTimeLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.CompositeBoxGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ZoomableBoxLocas extends ZoomableBoxTimeLocas<ZoomableBoxMetricsEntity> {


    public ZoomableBoxLocas(UUID teamId, UUID torchieId, FeatureCache featureCache,
                            InputStrategy<ZoomableBoxMetricsEntity> input,
                            OutputStrategy output) {
        super(teamId, torchieId, featureCache, input, output);

    }

    @Override
    protected void fillCompositeZoomGrid(CompositeBoxGrid boxZoomGrid, FeatureCache featureCache, List<ZoomableBoxMetricsEntity> boxInputMetrics) {

        for (ZoomableBoxMetricsEntity boxMetrics : boxInputMetrics) {

            PlaceReference boxReference = featureCache.lookupBoxReferenceWithUri(boxMetrics.getBoxFeatureId(), boxMetrics.getBoxUri());

            RelativeBeat beat = boxZoomGrid.getBeat(boxMetrics.getGridTime());
            Duration durationWeight = Duration.ofSeconds(boxMetrics.getTimeInBox());

            //need box as group here...
            boxZoomGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_WTF, beat, durationWeight, boxMetrics.getPercentWtf());
            boxZoomGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_LEARNING, beat, durationWeight, boxMetrics.getPercentLearning());
            boxZoomGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_PROGRESS, beat, durationWeight, boxMetrics.getPercentProgress());
            boxZoomGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_PERCENT_PAIRING, beat, durationWeight, boxMetrics.getPercentPairing());
            boxZoomGrid.addBoxMetric(boxReference,
                    MetricRowKey.ZOOM_AVG_FLAME, beat, durationWeight, boxMetrics.getAvgFlame());

        }

    }




}
