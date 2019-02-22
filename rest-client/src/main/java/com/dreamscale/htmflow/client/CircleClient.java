package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.circle.ChatMessageInputDto;
import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.circle.CreateWTFCircleInputDto;
import com.dreamscale.htmflow.api.circle.FeedMessageDto;
import feign.Headers;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface CircleClient {

    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH )
    CircleDto createNewAdhocWTFCircle(CreateWTFCircleInputDto circleSessionInputDto);


    @RequestLine("POST " + ResourcePaths.CIRCLE_PATH +  ResourcePaths.WTF_PATH + ResourcePaths.CHAT_PATH)
    FeedMessageDto postChatMessageToCircleFeed(ChatMessageInputDto chatMessageInputDto);


}
