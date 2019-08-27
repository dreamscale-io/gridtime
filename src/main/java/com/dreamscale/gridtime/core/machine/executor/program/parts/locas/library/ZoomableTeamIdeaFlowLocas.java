package com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library;

import com.dreamscale.gridtime.core.domain.tile.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.ZoomableTeamLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.memory.grid.TeamGrid;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Slf4j
public class ZoomableTeamIdeaFlowLocas extends ZoomableTeamLocas<ZoomableTeamIdeaFlowMetricsEntity> {


    public ZoomableTeamIdeaFlowLocas(UUID teamId,
                                     InputStrategy<ZoomableTeamIdeaFlowMetricsEntity> input,
                                     OutputStrategy output) {
        super(teamId, input, output);

    }

    @Override
    protected void fillTeamGrid(TeamGrid teamGrid, List<ZoomableTeamIdeaFlowMetricsEntity> ideaflowInputs) {

        for (ZoomableTeamIdeaFlowMetricsEntity ideaflow : ideaflowInputs) {

            Duration durationWeight = Duration.ofSeconds(ideaflow.getTimeInTile());

            teamGrid.addColumn(ideaflow.getTorchieId(), toInitials(ideaflow.getMemberName()));

            teamGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_WTF, durationWeight, ideaflow.getPercentWtf());
            teamGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_LEARNING, durationWeight, ideaflow.getPercentLearning());
            teamGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_PROGRESS, durationWeight, ideaflow.getPercentProgress());
            teamGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_PERCENT_PAIRING, durationWeight, ideaflow.getPercentProgress());
            teamGrid.addWeightedMetric(ideaflow.getTorchieId(), MetricRowKey.ZOOM_AVG_FLAME, durationWeight, ideaflow.getAvgFlame());

            teamGrid.addTimeForColumn(ideaflow.getTorchieId(), durationWeight);

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
