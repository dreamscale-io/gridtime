package com.dreamscale.htmflow.core.feeds.story.see;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.feature.band.BandLayerType;
import com.dreamscale.htmflow.core.feeds.story.feature.band.CircleContext;

import java.util.List;

/**
 * Translates the Circle Feed messages of start/stop shelf/resume on WTF Circles to TimeBands
 */
public class TroubleshootingStateObserver implements FlowObserver {

    @Override
    public void see(StoryFrame currentStoryFrame, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableCircleMessageEvent) {
                CircleFeedMessageEntity circleMessage = ((CircleFeedMessageEntity) flowable.get());

                CircleMessageType circleMessageType = circleMessage.getMessageType();

                if (isCircleOpening(circleMessageType)) {
                    currentStoryFrame.startBand(BandLayerType.FRICTION_TROUBLESHOOTING, circleMessage.getPosition(), createCircleContext(circleMessage));
                }

                if (isCircleEnding(circleMessageType)) {
                    currentStoryFrame.clearBand(BandLayerType.FRICTION_TROUBLESHOOTING, circleMessage.getPosition());
                }

            }
        }

        currentStoryFrame.finishAfterLoad();
    }

    private CircleContext createCircleContext(CircleFeedMessageEntity circleFeedMessageEntity) {
        return new CircleContext(circleFeedMessageEntity.getCircleId());
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
