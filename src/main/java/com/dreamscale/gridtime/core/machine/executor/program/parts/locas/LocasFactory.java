package com.dreamscale.gridtime.core.machine.executor.program.parts.locas;

import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamBoxMetricsEntity;
import com.dreamscale.gridtime.core.domain.tile.zoomable.ZoomableTeamIdeaFlowMetricsEntity;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableBoxLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableTeamBoxLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableTeamIdeaFlowLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.input.InputStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.ZoomableIdeaFlowLocas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.library.output.OutputStrategyFactory;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCacheManager;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.BoxMetrics;
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
    FeatureCacheManager featureCacheManager;

    public ZoomableIdeaFlowLocas createIdeaFlowAggregatorLocas(UUID teamId, UUID torchieId) {
        InputStrategy<ZoomableIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(teamId);

        return new ZoomableIdeaFlowLocas(teamId, torchieId, featureCache, ideaflowIn, ideaflowOut);
    }

    public ZoomableTeamIdeaFlowLocas createIdeaFlowTeamAggregatorLocas(UUID teamId) {
        InputStrategy<ZoomableTeamIdeaFlowMetricsEntity> ideaflowIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_TEAM_IDEA_FLOW_METRICS);
        OutputStrategy ideaflowOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_IDEA_FLOW_METRICS);

        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(teamId);

        return new ZoomableTeamIdeaFlowLocas(teamId, featureCache, ideaflowIn, ideaflowOut);
    }

    public ZoomableBoxLocas createBoxAggregatorLocas(UUID teamId, UUID torchieId) {
        InputStrategy<ZoomableBoxMetricsEntity> boxMetricsIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_BOX_METRICS);
        OutputStrategy boxMetricsOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_BOX_METRICS);
        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(teamId);

        return new ZoomableBoxLocas(teamId, torchieId, featureCache, boxMetricsIn, boxMetricsOut);
    }

    public ZoomableTeamBoxLocas createTeamBoxAggregatorLocas(UUID teamId) {
        InputStrategy<ZoomableTeamBoxMetricsEntity> boxMetricsIn = inputStrategyFactory.get(InputStrategyFactory.InputType.QUERY_TEAM_BOX_METRICS);
        OutputStrategy boxMetricsOut = outputStrategyFactory.get(OutputStrategyFactory.OutputType.SUMMARIZE_BOX_METRICS);

        FeatureCache featureCache = featureCacheManager.findOrCreateFeatureCache(teamId);
        return new ZoomableTeamBoxLocas(teamId, featureCache, boxMetricsIn, boxMetricsOut);
    }
}
