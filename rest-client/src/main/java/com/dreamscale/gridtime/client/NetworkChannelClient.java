package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.UserContextDto;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.network.ChannelMessageDto;
import com.dreamscale.gridtime.api.network.ChatMessageInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface NetworkChannelClient {


    @RequestLine("POST " + ResourcePaths.NETWORK_PATH + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.EMIT_PATH)
    ChannelMessageDto postChatMessageToChannel(@Param("id") String channelId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST "  + ResourcePaths.NETWORK_PATH+ ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.JOIN_PATH)
    SimpleStatusDto joinChannel(@Param("id") String channelId);

    @RequestLine("POST "  + ResourcePaths.NETWORK_PATH+ ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.LEAVE_PATH)
    SimpleStatusDto leaveChannel(@Param("id") String channelId);

    @RequestLine("GET "  + ResourcePaths.NETWORK_PATH + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.MEMBER_PATH )
    List<UserContextDto> getActiveChannelMembers(@Param("id") String channelId);

    @RequestLine("GET " + ResourcePaths.NETWORK_PATH + ResourcePaths.CHANNEL_PATH  + "/{id}" + ResourcePaths.MESSAGE_PATH)
    List<ChannelMessageDto> getAllChannelMessages(@Param("id") String channelId);

}
