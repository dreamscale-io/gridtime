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

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH )
    CircleDto createNewAdhocWTFCircle(CreateWTFCircleInputDto circleSessionInputDto);


    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH + ResourcePaths.CHAT_PATH)
    FeedMessageDto postChatMessageToCircleFeed(ChatMessageInputDto chatMessageInputDto);

    @RequestLine("GET " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH + ResourcePaths.FEED_PATH+"?circle_id={circleId}")
    List<FeedMessageDto> getAllMessagesForCircleFeed(@Param("circleId") String circleId );



}
