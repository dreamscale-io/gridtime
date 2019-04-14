package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.CircleDetails;

import java.util.List;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class WTFStateObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableCircleMessageEvent) {
                CircleFeedMessageEntity circleMessage = ((CircleFeedMessageEntity) flowable.get());

                CircleMessageType circleMessageType = circleMessage.getMessageType();

                if (isCircleOpening(circleMessageType)) {
                    currentStoryTile.startWTF(circleMessage.getPosition(), createCircleContext(circleMessage));
                }

                if (isCircleEnding(circleMessageType)) {
                    currentStoryTile.clearWTF(circleMessage.getPosition());
                }

            }
        }

        currentStoryTile.finishAfterLoad();
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
