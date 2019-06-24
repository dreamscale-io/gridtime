package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.executor.job.IdeaFlowChainJob;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.fetch.FetchStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.PerProcessFeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.search.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.sink.SinkStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.transform.FlowTransformFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TorchieFactory {


    @Autowired
    private FetchStrategyFactory fetchStrategyFactory;

    @Autowired
    private FlowObserverFactory flowObserverFactory;

    @Autowired
    private FlowTransformFactory flowTransformFactory;

    @Autowired
    private SinkStrategyFactory sinkStrategyFactory;

    @Autowired
    private FeatureSearchService featureSearchService;

    @Autowired
    private TileSearchService tileSearchService;

    private Map<UUID, FeatureCache> teamCacheMap = new HashMap<>();


    private FeatureCache findOrCreateFeatureCache(UUID teamId) {
        FeatureCache featureCache = teamCacheMap.get(teamId);
        if (featureCache == null) {
            featureCache = new FeatureCache();
            teamCacheMap.put(teamId, featureCache);
        }
        return featureCache;
    }

    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, memberId,
                featureCache, featureSearchService, tileSearchService);

        //stream data into the tiles
        IdeaFlowChainJob job = createIdeaFlowGeneratorJob(memberId, featurePool, startingPosition);

        return new Torchie(memberId, featurePool, job);

    }

    private IdeaFlowChainJob createIdeaFlowGeneratorJob(UUID memberId, PerProcessFeaturePool featurePool, LocalDateTime startingPosition) {
        IdeaFlowChainJob job = new IdeaFlowChainJob(memberId, featurePool, startingPosition);

        job.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.JOURNAL_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_FEELS_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_AUTHOR_OBSERVER));

        job.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER));

        job.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.EXECUTION_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.EXECUTION_RHYTHM_OBSERVER));

        job.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.CIRCLE_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER));

        //transformation only, with no new data

        job.addFlowTransformerToPullChain(
                flowTransformFactory.get(FlowTransformFactory.TransformType.RESOLVE_FEATURES_TRANSFORM));

        //save off the data in the tiles to permanent stores

        job.addFlowSinkToPullChain(
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_TO_POSTGRES),
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_BOOKMARK));

        return job;
    }


}
