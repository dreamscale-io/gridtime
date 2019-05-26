package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails;

import java.util.List;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class WTFStateObserver implements FlowObserver<FlowableCircleMessageEvent> {

    @Override
    public void seeInto(List<FlowableCircleMessageEvent> flowables, TileBuilder tileBuilder) {

        for (Flowable flowable : flowables) {
            CircleFeedMessageEntity circleMessage = (flowable.get());

            CircleMessageType circleMessageType = circleMessage.getMessageType();

            if (isCircleOpening(circleMessageType)) {
                tileBuilder.startWTF(circleMessage.getPosition(), createCircleContext(circleMessage));
            }

            if (isCircleEnding(circleMessageType)) {
                tileBuilder.clearWTF(circleMessage.getPosition());
            }
        }

        tileBuilder.finishAfterLoad();
    }

    private CircleDetails createCircleContext(CircleFeedMessageEntity circleFeedMessageEntity) {
        return new CircleDetails(circleFeedMessageEntity.getCircleId(), circleFeedMessageEntity.getCircleName());
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
