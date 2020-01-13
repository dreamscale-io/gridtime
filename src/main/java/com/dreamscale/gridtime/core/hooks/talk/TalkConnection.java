package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.TalkMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public class TalkConnection {

    private final TalkClient talkClient;
    private final UUID connectionId;

    public TalkConnection(TalkClient talkClient, UUID connectionId) {
        this.talkClient = talkClient;
        this.connectionId = connectionId;
    }

    public SimpleStatusDto joinRoom(String talkRoomId) {
        return talkClient.joinRoom(talkRoomId, new ClientConnectionDto(connectionId));
    }

    public SimpleStatusDto leaveRoom(String talkRoomId) {
        return talkClient.leaveRoom(talkRoomId, new ClientConnectionDto(connectionId));
    }


    public SimpleStatusDto sendDirectMessage(UUID messageId, UUID toConnectionId, LocalDateTime messageTime, Long nanoTime, String message) {
        return talkClient.sendDirectMessage(toConnectionId.toString(), new TalkMessageDto(messageId, toDirectUri(toConnectionId.toString()),
                messageTime, nanoTime, Collections.emptyMap(), TalkMessageType.CHAT.getTalkMessageType(),
                JSONTransformer.toJson(new ChatMessageDetailsDto(message))));
    }

    public SimpleStatusDto sendRoomMessage(UUID messageId, String talkRoomId, LocalDateTime messageTime, Long nanoTime, String message) {
        return talkClient.sendRoomMessage(talkRoomId, new TalkMessageDto(messageId, toRoomUri(talkRoomId), messageTime, nanoTime, Collections.emptyMap(),
                TalkMessageType.CHAT.getTalkMessageType(), JSONTransformer.toJson(new ChatMessageDetailsDto(message))));
    }

    private String toDirectUri(String clientId) {
        return clientId;
    }

    private String toRoomUri(String talkRoomId) {
        return talkRoomId;
    }

}


