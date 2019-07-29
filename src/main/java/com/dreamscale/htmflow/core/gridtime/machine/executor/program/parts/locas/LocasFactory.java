package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas;

import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.in.BreatheInStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.in.BreatheInStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.out.BreatheOutStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.out.BreatheOutStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LocasFactory {

    @Autowired
    BreatheInStrategyFactory breatheInFactory;

    @Autowired
    BreatheOutStrategyFactory breatheOutFactory;

    public IdeaFlowAggregatorLocas createIdeaFlowAggregatorLocas(UUID torchieId, FeatureCache featureCache) {
        BreatheInStrategy<IdeaFlowMetrics> metricsIn = breatheInFactory.get(BreatheInStrategyFactory.BreatheInType.QUERY_IDEA_FLOW_METRICS);
        BreatheOutStrategy metricsOut = breatheOutFactory.get(BreatheOutStrategyFactory.BreatheOutType.SUMMARIZE_IDEA_FLOW_METRICS);

        return new IdeaFlowAggregatorLocas(torchieId, featureCache, metricsIn, metricsOut);
    }
}
