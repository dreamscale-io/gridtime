package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.ActiveUserContextDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.channel.ChannelMessageDto;
import com.dreamscale.gridtime.api.channel.ChatMessageInputDto;
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
    ChannelMessageDto postChatMessageToChannel(@Param("id") String channelId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST " + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.JOIN_PATH)
    SimpleStatusDto joinChannel(@Param("id") String channelId);

    @RequestLine("POST " + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.LEAVE_PATH)
    SimpleStatusDto leaveChannel(@Param("id") String channelId);

    @RequestLine("GET " + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.MEMBER_PATH)
    List<ActiveUserContextDto> getActiveChannelMembers(@Param("id") String channelId);

    @RequestLine("GET " + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.MESSAGE_PATH)
    List<ChannelMessageDto> getAllChannelMessages(@Param("id") String channelId);

}
