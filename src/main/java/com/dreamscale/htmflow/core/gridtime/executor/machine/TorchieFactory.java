package com.dreamscale.htmflow.core.gridtime.executor.machine;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.executor.memory.PerProcessFeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.FeatureSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.memory.search.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.SinkStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform.FlowTransformFactory;
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

    public Torchie wireUpTeamTorchie(UUID teamId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, teamId,
                featureCache, featureSearchService, tileSearchService);

        Torchie torchie = new Torchie(teamId, featurePool, startingPosition);

        //tile source
        //tile transform is aggregation of a window

        //tile sink is saving of a team tile

        return torchie;
    }

    private FeatureCache findOrCreateFeatureCache(UUID teamId) {
        FeatureCache featureCache = teamCacheMap.get(teamId);
        if (featureCache == null) {
            featureCache = new FeatureCache();
            teamCacheMap.put(teamId, featureCache);
        }
        return featureCache;
    }

    //


    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, memberId,
                featureCache, featureSearchService, tileSearchService);

        Torchie torchie = new Torchie(memberId, featurePool, startingPosition);

        //stream data into the tiles

        torchie.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.JOURNAL_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_FEELS_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_AUTHOR_OBSERVER));

        torchie.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER));

        torchie.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.EXECUTION_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.EXECUTION_RHYTHM_OBSERVER));

        torchie.addFlowSourceToPullChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.CIRCLE_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER));

        //transformation only, with no new data

        torchie.addFlowTransformerToPullChain(
                flowTransformFactory.get(FlowTransformFactory.TransformType.URI_ASSIGNMENT_TRANSFORM));

        //save off the data in the tiles to permanent stores

        torchie.addFlowSinkToPullChain(
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_TO_POSTGRES),
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_BOOKMARK));

        return torchie;

    }



}
