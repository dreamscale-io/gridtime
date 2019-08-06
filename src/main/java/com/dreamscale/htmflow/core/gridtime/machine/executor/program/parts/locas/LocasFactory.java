package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.domain.tile.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.domain.tile.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.ZoomableTeamIdeaFlowLocas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.input.InputStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.ZoomableIdeaFlowLocas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.library.output.OutputStrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocasFactory {

    @Autowired
    InputStrategyFactory inputStrategyFactory;

    @Autowired
    OutputStrategyFactory outputStrategyFactory;

    public ZoomableIdeaFlowLocas createIdeaFlowAggregatorLocas(UUID torchieId) {
        InputStrategy<ZoomableIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new ZoomableIdeaFlowLocas(torchieId, ideaflowIn, ideaflowOut);
    }

    public ZoomableTeamIdeaFlowLocas createIdeaFlowTeamAggregatorLocas(UUID teamId) {
        InputStrategy<ZoomableTeamIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_TEAM_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new ZoomableTeamIdeaFlowLocas(teamId, ideaflowIn, ideaflowOut);
    }
}
