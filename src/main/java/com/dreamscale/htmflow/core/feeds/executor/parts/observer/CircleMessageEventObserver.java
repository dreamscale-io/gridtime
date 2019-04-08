package com.dreamscale.htmflow.core.feeds.executor.parts.observer;

import com.dreamscale.htmflow.api.circle.CircleMessageType;
import com.dreamscale.htmflow.core.domain.circle.CircleFeedMessageEntity;
import com.dreamscale.htmflow.core.feeds.common.Flowable;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.flowable.FlowableCircleMessageEvent;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.executor.parts.source.Window;
import com.dreamscale.htmflow.core.feeds.story.feature.details.IdeaDetails;

import java.util.List;

/**
 * Translates the flame ratings on JournalEntries to TimeBands with a FeelsContext that always fits within
 * the frame and contributes to the push/pull signals generated by all the flames combined
 */
public class CircleMessageEventObserver implements FlowObserver {

    @Override
    public void see(StoryTile currentStoryTile, Window window) {

        List<Flowable> flowables = window.getFlowables();

        for (Flowable flowable : flowables) {
            if (flowable instanceof FlowableCircleMessageEvent) {
                CircleFeedMessageEntity circleMessage = ((CircleFeedMessageEntity) flowable.get());

                CircleMessageType circleMessageType = circleMessage.getMessageType();

                if (isScrapbookEvent(circleMessageType)) {
                    currentStoryTile.shareAnIdea(circleMessage.getPosition(), createIdeaDeails(circleMessage));
                }

            }

            currentStoryTile.finishAfterLoad();

        }

    }

    private IdeaDetails createIdeaDeails(CircleFeedMessageEntity circleMessage) {
        IdeaDetails ideaDetails = new IdeaDetails();

        ideaDetails.setMessageFromTorchieId(circleMessage.getTorchieId());
        ideaDetails.setMessageFromName(circleMessage.getFullName());

        ideaDetails.setCircleId(circleMessage.getCircleId());
        ideaDetails.setCircleName(circleMessage.getCircleName());

        ideaDetails.setMessageId(circleMessage.getId());
        ideaDetails.setMessageType(circleMessage.getMessageType());
        ideaDetails.setMessage(circleMessage.getMessage());
        ideaDetails.setFileName(circleMessage.getFileName());
        ideaDetails.setFilePath(circleMessage.getFilePath());
        ideaDetails.setSnippetSource(circleMessage.getSnippetSource());
        return ideaDetails;
    }

    private boolean isScrapbookEvent(CircleMessageType circleMessageType) {
        return circleMessageType.equals(CircleMessageType.CHAT)
                || circleMessageType.equals(CircleMessageType.SCREENSHOT)
                || circleMessageType.equals(CircleMessageType.SNIPPET);
    }
}
