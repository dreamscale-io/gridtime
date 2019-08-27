package com.dreamscale.gridtime.core.hooks.realtime;

import com.dreamscale.gridtime.core.hooks.realtime.dto.ChannelMembersInputDto;
import com.dreamscale.gridtime.core.hooks.realtime.dto.ChatMessageInputDto;
import com.dreamscale.gridtime.core.hooks.realtime.dto.MemberInputDto;

import java.util.List;
import java.util.UUID;

public class RealtimeConnection {

    private final RealtimeClient realtimeClient;

    public RealtimeConnection(RealtimeClient realtimeClient) {
        this.realtimeClient = realtimeClient;
    }

    public void joinChannel(UUID channelId, UUID organizationId, UUID teamId, UUID memberId) {
        realtimeClient.joinChannel(channelId.toString(), new MemberInputDto(organizationId, teamId, memberId));
    }

    public void leaveChannel(UUID channelId, UUID organizationId, UUID teamId, UUID memberId) {
        realtimeClient.leaveChannel(channelId.toString(), new MemberInputDto(organizationId, teamId, memberId));
    }

    public void resyncChannel(UUID channelId, List<MemberInputDto> channelMembers) {
        realtimeClient.resyncChannel(channelId.toString(), new ChannelMembersInputDto(channelMembers));
    }

    public void postChatMessage(UUID channelId, UUID fromMemberId, String message) {
        realtimeClient.postChatMessage(channelId.toString(), new ChatMessageInputDto(fromMemberId, message));
    }
}


