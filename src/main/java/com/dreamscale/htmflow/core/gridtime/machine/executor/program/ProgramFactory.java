package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.SinkStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.FlowTransformFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class ProgramFactory {

    @Autowired
    private FeedStrategyFactory feedStrategyFactory;

    @Autowired
    private FlowObserverFactory flowObserverFactory;

    @Autowired
    private FlowTransformFactory flowTransformFactory;

    @Autowired
    private SinkStrategyFactory sinkStrategyFactory;

    @Autowired
    private CalendarService calendarService;

    public Program createBaseTileGeneratorProgram(UUID torchieId, FeaturePool featurePool, LocalDateTime startPosition) {

        TileGeneratorProgram program = new TileGeneratorProgram(torchieId, featurePool, startPosition);

        program.addFlowSourceToPullChain(
                feedStrategyFactory.get(FeedStrategyFactory.FeedType.JOURNAL_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_FEELS_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_AUTHOR_OBSERVER));

        program.addFlowSourceToPullChain(
                feedStrategyFactory.get(FeedStrategyFactory.FeedType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER));

        program.addFlowSourceToPullChain(
                feedStrategyFactory.get(FeedStrategyFactory.FeedType.EXECUTION_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.EXECUTION_RHYTHM_OBSERVER));

        program.addFlowSourceToPullChain(
                feedStrategyFactory.get(FeedStrategyFactory.FeedType.CIRCLE_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER));

        //transformation only, with no new data

        program.addFlowTransformerToPullChain(
                flowTransformFactory.get(FlowTransformFactory.TransformType.RESOLVE_FEATURES_TRANSFORM));

        //save off the data in the tiles to permanent stores

        program.addFlowSinkToPullChain(
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_TO_POSTGRES),
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_BOOKMARK));


        //proogram add aggregate chain
        //then generate different sort of instructions with alt chain

        //maybe if I detach from the feature pool, we will be good



        return program;
    }


    public CalendarGeneratorProgram createCalendarGenerator(int maxTiles) {

        return new CalendarGeneratorProgram(calendarService, maxTiles);
    }

//    public AggregateByTeamProgram createAggregateWiresProgram(UUID teamId, PerProcessFeaturePool featurePool, AggregatingWire teamWire) {
//
//        return new AggregateByTeamProgram(teamId, featurePool, teamWire);
//    }


}
