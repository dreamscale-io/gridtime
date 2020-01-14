package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

public class TalkConnection {

    private final TalkClient talkClient;

    public TalkConnection(TalkClient talkClient) {
        this.talkClient = talkClient;
    }

    public SimpleStatusDto joinRoom(UUID connectionId, UUID roomId) {
        return talkClient.joinRoom(roomId.toString(), new ClientConnectionDto(connectionId));
    }

    public SimpleStatusDto leaveRoom(UUID connectionId, UUID roomId) {
        return talkClient.leaveRoom(roomId.toString(), new ClientConnectionDto(connectionId));
    }

    public SimpleStatusDto sendDirectMessage(UUID toConnectionId, TalkMessageDto talkMessageDto) {
        return talkClient.sendDirectMessage(toConnectionId.toString(), talkMessageDto);
    }

    public SimpleStatusDto sendRoomMessage(UUID roomId, TalkMessageDto talkMessageDto) {
        return talkClient.sendRoomMessage(roomId.toString(), talkMessageDto);
    }


    private String toDirectUri(String clientId) {
        return clientId;
    }

    private String toRoomUri(String talkRoomId) {
        return talkRoomId;
    }

}


