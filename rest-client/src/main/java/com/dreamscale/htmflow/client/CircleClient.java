package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.circle.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface CircleClient {


    @RequestLine("GET " + ResourcePaths.CIRCLE_PATH )
    List<CircleDto> getAllOpenCircles();

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH )
    CircleDto createNewAdhocWTFCircle(CreateWTFCircleInputDto circleSessionInputDto);

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.CHAT_PATH)
    FeedMessageDto postChatMessageToCircleFeed(@Param("id") String circleId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("GET " + ResourcePaths.CIRCLE_PATH + "/{id}" + ResourcePaths.FEED_PATH)
    List<FeedMessageDto> getAllMessagesForCircleFeed(@Param("id") String circleId );

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH  + "/{id}" + ResourcePaths.CLOSE_PATH)
    void closeCircle(@Param("id") String circleId);
}
