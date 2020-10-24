package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableBoxLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableTeamBoxLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableTeamIdeaFlowLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.AggregateInputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableIdeaFlowLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategyFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocasFactory {

    @Autowired
    InputStrategyFactory inputStrategyFactory;

    @Autowired
    OutputStrategyFactory outputStrategyFactory;

    @Autowired
    FeatureCache featureCache;

    public ZoomableIdeaFlowLocas createIdeaFlowAggregatorLocas( UUID torchieId) {
        InputStrategy<ZoomableIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new ZoomableIdeaFlowLocas(torchieId, featureCache, ideaflowIn, ideaflowOut);
    }

    public ZoomableTeamIdeaFlowLocas createIdeaFlowTeamAggregatorLocas(UUID teamId) {
        AggregateInputStrategy<ZoomableTeamIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.getAggregate(InputStrategyFactory.InputType.QUERY_TEAM_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new ZoomableTeamIdeaFlowLocas(teamId, featureCache, ideaflowIn, ideaflowOut);
    }

    public ZoomableBoxLocas createBoxAggregatorLocas( UUID torchieId) {
        InputStrategy<ZoomableBoxMetricsEntity> boxMetricsIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_BOX_METRICS);
        OutputStrategy boxMetricsOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_BOX_METRICS);

        return new ZoomableBoxLocas(torchieId, featureCache, boxMetricsIn, boxMetricsOut);
    }

    public ZoomableTeamBoxLocas createTeamBoxAggregatorLocas(UUID teamId) {
        AggregateInputStrategy<ZoomableTeamBoxMetricsEntity> boxMetricsIn = inputStrategyFactory.getAggregate(InputStrategyFactory.InputType.QUERY_TEAM_BOX_METRICS);
        OutputStrategy boxMetricsOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_BOX_METRICS);

        return new ZoomableTeamBoxLocas(teamId, featureCache, boxMetricsIn, boxMetricsOut);
    }
}
