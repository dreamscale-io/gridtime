package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
public class TalkConnection {

    private final TalkClient talkClient;

    public TalkConnection(TalkClient talkClient) {
        this.talkClient = talkClient;
    }

    public SimpleStatusDto joinRoom(UUID connectionId, UUID roomId) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.joinRoom(roomId.toString(), new ClientConnectionDto(connectionId));
        } catch (Exception ex) {
            log.error("TalkConnection.joinRoom", ex);
            status = new SimpleStatusDto(Status.FAILED, "TalkConnection.joinRoom "+ex.getMessage());
        }

        return status;
    }


    public SimpleStatusDto leaveRoom(UUID connectionId, UUID roomId) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.leaveRoom(roomId.toString(), new ClientConnectionDto(connectionId));
        } catch (Exception ex) {
            log.error("TalkConnection.leaveRoom failed", ex);
            status = new SimpleStatusDto(Status.FAILED, "TalkConnection.leaveRoom: "+ex.getMessage());
        }

        return status;
    }

    public SimpleStatusDto sendDirectMessage(UUID toConnectionId, TalkMessageDto talkMessageDto) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.sendDirectMessage(toConnectionId.toString(), talkMessageDto);
        } catch (Exception ex) {
            log.error("TalkConnection.sendDirectMessage failed", ex);
            status = new SimpleStatusDto(Status.FAILED, "TalkConnection.sendDirectMessage failed: "+ex.getMessage());
        }

        return status;
    }

    public SimpleStatusDto sendRoomMessage(UUID roomId, TalkMessageDto talkMessageDto) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.sendRoomMessage(roomId.toString(), talkMessageDto);
        } catch (Exception ex) {
            log.error("TalkConnection.sendRoomMessage failed", ex);
            status = new SimpleStatusDto(Status.FAILED, "TalkConnection.sendRoomMessage failed: "+ex.getMessage());
        }

        return status;
    }


    private String toDirectUri(String clientId) {
        return clientId;
    }

    private String toRoomUri(String talkRoomId) {
        return talkRoomId;
    }

}


