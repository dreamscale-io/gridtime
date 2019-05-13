package com.dreamscale.htmflow.core.feeds.executor;

import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.FetchStrategyFactory;
import com.dreamscale.htmflow.core.feeds.executor.parts.observer.FlowObserverFactory;
import com.dreamscale.htmflow.core.feeds.executor.parts.sink.SinkStrategyFactory;
import com.dreamscale.htmflow.core.feeds.executor.parts.transform.FlowTransformFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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

    public Torchie wireUpTeamTorchie(UUID teamId, LocalDateTime startingPosition) {

        Torchie torchie = new Torchie(teamId, startingPosition);

        //tile source
        //tile transform is aggregation of a window

        //tile sink is saving of a team tile

        return torchie;
    }

    public Torchie wireUpMemberTorchie(UUID memberId, LocalDateTime startingPosition) {

        Torchie torchie = new Torchie(memberId, startingPosition);

        //stream data into the tiles

        torchie.addFlowSourceToChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.JOURNAL_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_CONTEXT_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_FEELS_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.JOURNAL_AUTHOR_OBSERVER));

        torchie.addFlowSourceToChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.FILE_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.COMPONENT_SPACE_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.LEARNING_STATE_OBSERVER));

        torchie.addFlowSourceToChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.EXECUTION_ACTIVITY_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.EXECUTION_RHYTHM_OBSERVER));

        torchie.addFlowSourceToChain(
                fetchStrategyFactory.get(FetchStrategyFactory.FeedType.CIRCLE_MESSAGES_FEED),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.WTF_STATE_OBSERVER),
                flowObserverFactory.get(FlowObserverFactory.ObserverType.CIRCLE_MESSAGE_OBSERVER));

        //transformation only, with no new data

        torchie.addFlowTransformerToChain(
                flowTransformFactory.get(FlowTransformFactory.TransformType.URI_ASSIGNMENT_TRANSFORM),
                flowTransformFactory.get(FlowTransformFactory.TransformType.MUSIC_PLAYER_TRANSFORM));

        //save off the data in the tiles to permanent stores

        torchie.addFlowSinkToChain(
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_TO_POSTGRES),
                sinkStrategyFactory.get(SinkStrategyFactory.SinkType.SAVE_BOOKMARK));

        return torchie;

    }

}
