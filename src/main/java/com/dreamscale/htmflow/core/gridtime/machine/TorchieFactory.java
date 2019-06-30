package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.CalendarGeneratorProgram;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.PullChainProgram;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.FetchStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.service.BoxConfigurationLoaderService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.service.CalendarService;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.MemoryOnlyFeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.PerProcessFeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.FeatureResolverService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.service.TileSearchService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.SinkStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.FlowTransformFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.box.TeamBoxConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private FeatureResolverService featureResolverService;

    @Autowired
    private TileSearchService tileSearchService;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    private BoxConfigurationLoaderService boxConfigurationLoaderService;

    private static final int MAX_TEAMS = 5;

    private Map<UUID, FeatureCache> teamCacheMap = DefaultCollections.lruMap(MAX_TEAMS);

    private FeatureCache findOrCreateFeatureCache(UUID teamId) {
        FeatureCache featureCache = teamCacheMap.get(teamId);
        if (featureCache == null) {
            TeamBoxConfiguration teamBoxConfig = boxConfigurationLoaderService.loadBoxConfiguration(teamId);

            featureCache = new FeatureCache(teamBoxConfig);
            teamCacheMap.put(teamId, featureCache);
        }
        return featureCache;
    }

    public Torchie wireUpMemberTorchie(UUID teamId, UUID memberId, LocalDateTime startingPosition) {
        FeatureCache featureCache = findOrCreateFeatureCache(teamId);

        PerProcessFeaturePool featurePool = new PerProcessFeaturePool(teamId, memberId,
                featureCache, featureResolverService, tileSearchService);

        //stream data into the tiles
        PullChainProgram job = createPullChainJob(memberId, featurePool, startingPosition);

        return new Torchie(memberId, featurePool, job);

    }

    public Torchie wireUpCalendarTorchie(int tilesToGenerate) {

        UUID torchieId = UUID.randomUUID();

        FeaturePool featurePool = new MemoryOnlyFeaturePool(torchieId);
        CalendarGeneratorProgram job = new CalendarGeneratorProgram(tilesToGenerate, calendarService);

        return new Torchie(torchieId, featurePool, job);

    }

    private PullChainProgram createPullChainJob(UUID memberId, PerProcessFeaturePool featurePool, LocalDateTime startingPosition) {
        PullChainProgram job = new PullChainProgram(memberId, featurePool, startingPosition);

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
