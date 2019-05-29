package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.FinishCircleTag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types.StartCircleTag;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.List;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class WTFStateObserver implements FlowObserver<FlowableCircleMessageEvent> {

    @Override
    public void seeInto(List<FlowableCircleMessageEvent> flowables, GridTile gridTile) {

        for (Flowable flowable : flowables) {
            CircleFeedMessageEntity circleMessage = (flowable.get());

            CircleMessageType circleMessageType = circleMessage.getMessageType();

            if (isCircleOpening(circleMessageType)) {
                gridTile.startWTF(circleMessage.getPosition(), createStartCircleTag(circleMessage));
            }

            if (isCircleEnding(circleMessageType)) {
                gridTile.clearWTF(circleMessage.getPosition(), createFinishCircleTag(circleMessage));
            }
        }

    }

    private FinishCircleTag createFinishCircleTag(CircleFeedMessageEntity circleMessage) {
        return new FinishCircleTag(circleMessage.getCircleId(), circleMessage.getCircleName(), circleMessage.getMessageType().name());
    }

    private StartCircleTag createStartCircleTag(CircleFeedMessageEntity circleMessage) {
        return new StartCircleTag(circleMessage.getCircleId(), circleMessage.getCircleName(), circleMessage.getMessageType().name());
    }


    private boolean isCircleOpening(CircleMessageType circleMessageType) {
        return circleMessageType.equals(CircleMessageType.CIRCLE_START)
                || circleMessageType.equals(CircleMessageType.CIRCLE_RESUMED);
    }

    private boolean isCircleEnding(CircleMessageType circleMessageType) {
        return circleMessageType.equals(CircleMessageType.CIRCLE_CLOSED)
                || circleMessageType.equals(CircleMessageType.CIRCLE_SHELVED);
    }
}
