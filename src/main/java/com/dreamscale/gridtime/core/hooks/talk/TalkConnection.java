package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;

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


    public void sendDirectMessage(UUID messageId, UUID toConnectionId, LocalDateTime messageTime, String message) {
//        talkClient.sendDirectMessage(new TalkMessageDto(messageId, connectionId.toString(), toConnectionId.toString(), messageTime,
//                TalkMessageType.CHAT.getTalkMessageType(), JSONTransformer.toJson(new ChatMessageDto(message))));
    }

    public void sendRoomMessage(UUID messageId, String talkRoomId, LocalDateTime messageTime, String message) {
//        talkClient.sendRoomMessage(talkRoomId, new TalkMessageDto(messageId, connectionId.toString(), talkRoomId, messageTime,
//                TalkMessageType.CHAT.getTalkMessageType(), JSONTransformer.toJson(new ChatMessageDto(message))));
    }

}


