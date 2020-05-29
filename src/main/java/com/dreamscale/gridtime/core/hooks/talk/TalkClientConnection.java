package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.core.exception.InternalErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.InternalServerException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Slf4j
public class TalkClientConnection {

    private final TalkClient talkClient;

    public TalkClientConnection(TalkClient talkClient) {
        this.talkClient = talkClient;
    }

    public SimpleStatusDto joinRoom(UUID connectionId, UUID roomId, String username, String roomName) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.joinRoom(roomId.toString(), new ClientConnectionDto(connectionId));
        } catch (Exception ex) {
            log.error("[TalkClient] joinRoom failed with exception.", ex);
            throw new InternalServerException(InternalErrorCodes.TALK_ERROR, "[TalkClient] joinRoom("+username + " , "+roomName+") failed with exception.");
        }

        return status;
    }

    public SimpleStatusDto leaveRoom(UUID connectionId, UUID roomId, String username, String roomName) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.leaveRoom(roomId.toString(), new ClientConnectionDto(connectionId));
        } catch (Exception ex) {
            log.error("[TalkClient] leaveRoom failed with exception.", ex);
            throw new InternalServerException(InternalErrorCodes.TALK_ERROR, "[TalkClient] leaveRoom("+username + ", "+roomName+") failed with exception.");

        }

        return status;
    }

    public SimpleStatusDto sendDirectMessage(UUID toConnectionId, TalkMessageDto talkMessageDto, String username) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.sendDirectMessage(toConnectionId.toString(), talkMessageDto);
        } catch (Exception ex) {
            log.error("[TalkClient] sendDirectMessage failed with exception.", ex);
            throw new InternalServerException(InternalErrorCodes.TALK_ERROR, "[TalkClient] sendDirectMessage("+username + ") failed with exception.");
        }

        return status;
    }

    public SimpleStatusDto sendRoomMessage(UUID roomId, TalkMessageDto talkMessageDto, String username, String roomName) {
        SimpleStatusDto status = null;

        try {
            status = talkClient.sendRoomMessage(roomId.toString(), talkMessageDto);
        } catch (Exception ex) {
            log.error("[TalkClient] sendRoomMessage failed with exception.", ex);
            throw new InternalServerException(InternalErrorCodes.TALK_ERROR, "[TalkClient] sendRoomMessage("+username + " , "+roomName+") failed with exception.");
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


