package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageType;
import com.dreamscale.gridtime.core.hooks.talk.dto.ChatMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;

import java.time.LocalDateTime;
import java.util.UUID;

public class TalkConnection {

    private final TalkClient talkClient;
    private final UUID connectionId;

    public TalkConnection(TalkClient talkClient, UUID connectionId) {
        this.talkClient = talkClient;
        this.connectionId = connectionId;
    }

    public void joinRoom(String talkRoomId) {
        talkClient.joinRoom(talkRoomId, new ClientConnectionDto(connectionId));
    }

    public void leaveRoom(String talkRoomId) {
        talkClient.leaveRoom(talkRoomId, new ClientConnectionDto(connectionId));
    }


    public void sendDirectMessage(UUID toConnectionId, LocalDateTime messageTime, String message) {
        talkClient.sendDirectMessage(new TalkMessageDto(connectionId.toString(), toConnectionId.toString(), messageTime,
                TalkMessageType.CHAT.getTalkMessageType(), JSONTransformer.toJson(new ChatMessageDto(message))));
    }

    public void sendRoomMessage(String talkRoomId, LocalDateTime messageTime, String message) {
        talkClient.sendRoomMessage(talkRoomId, new TalkMessageDto(connectionId.toString(), talkRoomId, messageTime,
                TalkMessageType.CHAT.getTalkMessageType(), JSONTransformer.toJson(new ChatMessageDto(message))));
    }

}


