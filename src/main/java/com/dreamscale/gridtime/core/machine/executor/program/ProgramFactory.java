package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorEntity;
import com.dreamscale.gridtime.core.domain.work.TorchieFeedCursorRepository;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.AggregateWorkToDoQueueWire;
import com.dreamscale.gridtime.core.machine.executor.job.CalendarJobDescriptor;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.service.CalendarService;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.LocasFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.observer.FlowObserverFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.sink.SinkStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.transform.FlowTransformFactory;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.cache.FeatureCache;
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
    private LocasFactory locasFactory;

    @Autowired
    private CalendarService calendarService;

    @Autowired
    AggregateWorkToDoQueueWire workToDoWire;

    @Autowired
    TorchieFeedCursorRepository torchieFeedCursorRepository;


    public Program createBaseTileGeneratorProgram(UUID torchieId, TorchieState torchieState, LocalDateTime startPosition, LocalDateTime runUntilPosition) {

        SourceTileGeneratorProgram program = new SourceTileGeneratorProgram(torchieId, torchieState, startPosition, runUntilPosition);

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
                feedStrategyFactory.get(FeedStrategyFactory.FeedType.WTF_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER));

        //transformation only, with no new data

        program.addFlowTransformerToPullChain(
                flowTransformFactory.get(FlowTransformFactory.TransformType.RESOLVE_FEATURES_TRANSFORM));

        //save off the data in the tiles to permanent stores

        program.addFlowSinkToPullChain(
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_TO_POSTGRES),
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_BOOKMARK));


        program.addAggregator(locasFactory.createIdeaFlowAggregatorLocas(torchieId));
        program.addAggregator(locasFactory.createBoxAggregatorLocas(torchieId));

        return program;
    }

    public AggregatePlexerProgram createAggregatePlexerProgram(UUID workerId) {
        return new AggregatePlexerProgram(workerId, workToDoWire, locasFactory);
    }



    public CalendarGeneratorProgram createCalendarGenerator(CalendarJobDescriptor jobDescriptor) {

        return new CalendarGeneratorProgram(calendarService, jobDescriptor.getCalendarJobStart(), jobDescriptor.getRunUntilDate());
    }



//    public AggregateByTeamProgram createAggregateWiresProgram(UUID teamId, PerProcessFeaturePool featurePool, AggregatingWire teamWire) {
//
//        return new AggregateByTeamProgram(teamId, featurePool, teamWire);
//    }


}
