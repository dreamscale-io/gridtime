package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.observer;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.FinishTypeTag;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.types.StartTypeTag;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.details.CircleDetails;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class WTFStateObserver implements FlowObserver<FlowableCircleMessageEvent> {

    @Override
    public void see(Window<FlowableCircleMessageEvent> window, GridTile gridTile) {

        for (Flowable flowable : window.getFlowables()) {
            CircleFeedMessageEntity circleMessage = (flowable.get());

            CircleMessageType circleMessageType = circleMessage.getMessageType();

            if (isCircleOpening(circleMessageType)) {
                gridTile.startWTF(circleMessage.getPosition(), createCircleDetails(circleMessage), decodeStartTag(circleMessage.getMessageType()));
            }

            if (isCircleEnding(circleMessageType)) {
                gridTile.clearWTF(circleMessage.getPosition(), decodeFinishTag(circleMessage.getMessageType()));
            }
        }
    }

    private CircleDetails createCircleDetails(CircleFeedMessageEntity circleMessage) {
        return new CircleDetails(circleMessage.getCircleId(), circleMessage.getCircleName());
    }


    private StartTypeTag decodeStartTag(CircleMessageType circleMessageType) {
        if (circleMessageType.equals(CircleMessageType.CIRCLE_START)) {
            return StartTypeTag.Start;
        } else if (circleMessageType.equals(CircleMessageType.CIRCLE_RESUMED)) {
            return StartTypeTag.Resume;
        }
        return StartTypeTag.Start;
    }

    private FinishTypeTag decodeFinishTag(CircleMessageType circleMessageType) {
        if (circleMessageType.equals(CircleMessageType.CIRCLE_CLOSED)) {
            return FinishTypeTag.Success;
        } else if (circleMessageType.equals(CircleMessageType.CIRCLE_SHELVED)) {
            return FinishTypeTag.DoItLater;
        }
        return FinishTypeTag.Success;
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
