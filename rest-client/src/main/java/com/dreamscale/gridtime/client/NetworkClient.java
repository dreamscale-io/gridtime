package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circle.*;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
import com.dreamscale.gridtime.api.network.MemberChannelsDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface NetworkClient {

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.AUTH_PATH + ResourcePaths.MEMBER_PATH + "/{id}" )
    MemberChannelsDto authorizeMemberToUseNetwork(@Param("id") String memberId);

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH )
    List<CircleDto> getAllOpenCircles();

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH + ResourcePaths.ACTIVE_PATH )
    CircleDto getActiveCircle();

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH + ResourcePaths.DO_IT_LATER_PATH)
    List<CircleDto> getAllDoItLaterCircles();

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH)
    CircleDto createNewAdhocWTFCircle(CreateWTFCircleInputDto circleSessionInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.FEED_PATH + ResourcePaths.CHAT_PATH)
    FeedMessageDto postChatMessageToCircleFeed(@Param("id") String circleId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.FEED_PATH + ResourcePaths.SCREENSHOT_PATH)
    FeedMessageDto postScreenshotReferenceToCircleFeed(@Param("id") String circleId, ScreenshotReferenceInputDto screenshotReferenceInputDto);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + ResourcePaths.ACTIVE_PATH + ResourcePaths.FEED_PATH + ResourcePaths.SNIPPET_PATH)
    FeedMessageDto postSnippetToActiveCircleFeed(NewSnippetEvent newSnippetEvent);

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH + "/{id}" + ResourcePaths.FEED_PATH)
    List<FeedMessageDto> getAllMessagesForCircleFeed(@Param("id") String circleId );

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.CLOSE_PATH)
    CircleDto closeCircle(@Param("id") String circleId);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.DO_IT_LATER_PATH)
    CircleDto shelveCircleWithDoItLater(@Param("id") String circleId);

    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.TRANSITION_PATH + ResourcePaths.RESUME_PATH)
    CircleDto resumeAnExistingCircleFromDoItLaterShelf(@Param("id") String circleId);
}
