package com.dreamscale.gridtime.core.hooks.realtime;

import com.dreamscale.gridtime.core.hooks.realtime.dto.ChannelMembersInputDto;
import com.dreamscale.gridtime.core.hooks.realtime.dto.ChatMessageInputDto;
import com.dreamscale.gridtime.core.hooks.realtime.dto.MemberInputDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface RealtimeClient {

    @RequestLine("POST "+ RealtimePaths.CHANNEL_PATH + "/{id}" + RealtimePaths.EMIT_PATH)
    void postChatMessage(@Param("id") String channelId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST "+ RealtimePaths.CHANNEL_PATH + "/{id}" + RealtimePaths.JOIN_PATH)
    void joinChannel(@Param("id") String channelId, MemberInputDto memberInputDto);

    @RequestLine("POST "+ RealtimePaths.CHANNEL_PATH + "/{id}" + RealtimePaths.LEAVE_PATH)
    void leaveChannel(@Param("id") String channelId,  MemberInputDto memberInputDto);

    @RequestLine("POST "+ RealtimePaths.CHANNEL_PATH + "/{id}" + RealtimePaths.RESYNC_PATH)
    void resyncChannel(@Param("id") String channelId,  ChannelMembersInputDto memberInputDto);
}
