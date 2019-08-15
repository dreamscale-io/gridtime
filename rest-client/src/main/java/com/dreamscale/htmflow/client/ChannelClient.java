package com.dreamscale.htmflow.client;

import com.dreamscale.htmflow.api.ResourcePaths;
import com.dreamscale.htmflow.api.account.SimpleStatusDto;
import com.dreamscale.htmflow.api.channel.ChatMessageInputDto;
import com.dreamscale.htmflow.api.circle.*;
import com.dreamscale.htmflow.api.event.NewSnippetEvent;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface ChannelClient {


    @RequestLine("POST " + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.MESSAGE_PATH)
    SimpleStatusDto postChatMessageToChannel(@Param("id") String channelId, ChatMessageInputDto chatMessageInputDto);

}
