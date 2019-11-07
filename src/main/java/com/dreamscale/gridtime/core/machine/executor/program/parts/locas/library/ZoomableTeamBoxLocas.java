package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableTeamLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamZoomGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Slf4j
public class ZoomableTeamBoxLocas extends ZoomableTeamLocas<ZoomableBoxMetricsEntity> {


    public ZoomableTeamBoxLocas(UUID teamId, FeatureCache featureCache,
                                InputStrategy<ZoomableBoxMetricsEntity> input,
                                OutputStrategy output) {
        super(teamId, featureCache, input, output);

    }

    @Override
    protected void fillTeamGrid(TeamZoomGrid teamZoomGrid, List<ZoomableBoxMetricsEntity> ideaflowInputs) {

        for (ZoomableBoxMetricsEntity ideaflow : ideaflowInputs) {



//            Duration durationWeight = Duration.ofSeconds(ideaflow.getTimeInTile());
//
//            teamMetricGrid.addColumn(ideaflow.getTorchieId(), toInitials(ideaflow.getMemberName()));
//
//            teamMetricGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_WTF, durationWeight, ideaflow.getPercentWtf());
//            teamMetricGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_LEARNING, durationWeight, ideaflow.getPercentLearning());
//            teamMetricGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_PROGRESS, durationWeight, ideaflow.getPercentProgress());
//            teamMetricGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_PAIRING, durationWeight, ideaflow.getPercentProgress());
//            teamMetricGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_AVG_FLAME, durationWeight, ideaflow.getAvgFlame());
//
//            teamMetricGrid.addTimeForColumn(ideaflow.getTorchieId(), durationWeight);

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
