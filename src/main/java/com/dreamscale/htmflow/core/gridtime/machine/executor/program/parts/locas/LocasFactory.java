package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.domain.tile.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input.InputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.input.InputStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output.OutputStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.output.OutputStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocasFactory {

    @Autowired
    InputStrategyFactory inputStrategyFactory;

    @Autowired
    OutputStrategyFactory outputStrategyFactory;

    public IdeaFlowAggregatorLocas createIdeaFlowAggregatorLocas(UUID torchieId) {
        InputStrategy<ZoomableIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new IdeaFlowAggregatorLocas(torchieId, ideaflowIn, ideaflowOut);
    }
}
