package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamBoxMetricsEntity;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableBoxTeamLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableTeamLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.CompositeTeamBoxGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamZoomGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ZoomableTeamBoxLocas extends ZoomableBoxTeamLocas<ZoomableTeamBoxMetricsEntity> {


    public ZoomableTeamBoxLocas(UUID teamId, FeatureCache featureCache,
                                InputStrategy<ZoomableTeamBoxMetricsEntity> input,
                                OutputStrategy output) {
        super(teamId, featureCache, input, output);

    }

    @Override
    protected void fillCompositeZoomGrid(CompositeTeamBoxGrid boxZoomGrid, FeatureCache featureCache, List<ZoomableTeamBoxMetricsEntity> metricInputs) {

        for (ZoomableTeamBoxMetricsEntity boxMetrics : metricInputs) {

            PlaceReference boxReference = featureCache.lookupBoxReferenceWithUri(boxMetrics.getBoxFeatureId(), boxMetrics.getBoxUri());

            RelativeBeat beat = boxZoomGrid.getBeat(boxMetrics.getGridTime());
            Duration durationWeight = Duration.ofSeconds(boxMetrics.getTimeInBox());

            String initials = toInitials(boxMetrics.getMemberName());

            //need box as group here...
            boxZoomGrid.addTeamBoxMetric(boxMetrics.getTorchieId(), initials, boxReference,
                    MetricRowKey.ZOOM_PERCENT_WTF, durationWeight, boxMetrics.getPercentWtf());
            boxZoomGrid.addTeamBoxMetric(boxMetrics.getTorchieId(), initials, boxReference,
                    MetricRowKey.ZOOM_PERCENT_LEARNING, durationWeight, boxMetrics.getPercentLearning());
            boxZoomGrid.addTeamBoxMetric(boxMetrics.getTorchieId(), initials, boxReference,
                    MetricRowKey.ZOOM_PERCENT_PROGRESS, durationWeight, boxMetrics.getPercentProgress());
            boxZoomGrid.addTeamBoxMetric(boxMetrics.getTorchieId(), initials, boxReference,
                    MetricRowKey.ZOOM_PERCENT_PAIRING, durationWeight, boxMetrics.getPercentPairing());
            boxZoomGrid.addTeamBoxMetric(boxMetrics.getTorchieId(), initials, boxReference,
                    MetricRowKey.ZOOM_AVG_FLAME, durationWeight, boxMetrics.getAvgFlame());

            boxZoomGrid.addTimeForColumn(boxMetrics.getTorchieId(), boxReference, durationWeight);


        }

    }


    private String toInitials(String memberName) {
        String [] nameParts = memberName.split(" ");
        String initials = "";
        for (String namePart : nameParts) {
            if (namePart.length() > 1) {
                initials += namePart.substring(0, 1).toUpperCase();
            }
        }

        return initials;
    }


}
