package com.dreamscale.gridtime.core.hooks.talk.dto;

import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto;
import com.dreamscale.gridtime.api.circuit.CircuitStatusDto;
import lombok.Getter;

@Getter
public enum CircuitMessageType {
    CHAT("chat", null, ChatMessageDetailsDto.class),
    SCREENSHOT("screenshot", null, null),
    SNIPPET("snippet", null, null),
    ROOM_MEMBER_JOIN("room-member-join", "Member joined.", CircuitMemberStatusDto.class),
    ROOM_MEMBER_INACTIVE("room-member-inactive", "Member inactive.", CircuitMemberStatusDto.class),
    ROOM_MEMBER_ACTIVE("room-member-active", "Member is active.", CircuitMemberStatusDto.class),
    CIRCUIT_OPEN("circuit-open", "Circuit is opened.", CircuitStatusDto.class),
    CIRCUIT_CLOSED("circuit-closed", "Circuit is closed.", CircuitStatusDto.class),
    CIRCUIT_ONHOLD("circuit-onhold", "Circuit is on hold.", CircuitStatusDto.class),
    CIRCUIT_RESUMED("circuit-resume", "Circuit is resumed.", CircuitStatusDto.class),
    CIRCUIT_RETRO_STARTED("circuit-retro", "Circuit retro is started.", CircuitStatusDto.class);

    private final String talkMessageType;
    private final Class<?> messageClazz;

    private final String statusMessage;

    CircuitMessageType(String talkMessageType, String statusMessage, Class<?> messageClazz) {
        this.talkMessageType = talkMessageType;
        this.messageClazz = messageClazz;
        this.statusMessage = statusMessage;
    }

    public String getSimpleClassName() {
        if (messageClazz != null) {
            return messageClazz.getSimpleName();
        }
        return "";
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
