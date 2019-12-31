package com.dreamscale.gridtime.core.hooks.talk.dto;

import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.CircuitStatusMessageDetailsDto;
import lombok.Getter;

@Getter
public enum TalkMessageType {
    CHAT("chat", null, ChatMessageDetailsDto.class),
    SCREENSHOT("screenshot", null, null),
    SNIPPET("snippet", null, null),
    ROOM_MEMBER_JOIN("room-member-join", "Member joined.", null),
    ROOM_MEMBER_INACTIVE("room-member-inactive", "Member inactive.", null),
    ROOM_MEMBER_ACTIVE("room-member-active", "Member is active.", null),
    CIRCUIT_OPEN("circuit-open", "Circuit is opened.", CircuitStatusMessageDetailsDto.class),
    CIRCUIT_CLOSED("circuit-closed", "Circuit is closed.", CircuitStatusMessageDetailsDto.class),
    CIRCUIT_ONHOLD("circuit-onhold", "Circuit is on hold.", CircuitStatusMessageDetailsDto.class),
    CIRCUIT_RESUMED("circuit-resume", "Circuit is resumed.", CircuitStatusMessageDetailsDto.class),
    CIRCUIT_RETRO_STARTED("circuit-retro", "Circuit retro is started.", CircuitStatusMessageDetailsDto.class);

    private final String talkMessageType;
    private final Class<?> messageClazz;

    private final String statusMessage;

    TalkMessageType(String talkMessageType, String statusMessage, Class<?> messageClazz) {
        this.talkMessageType = talkMessageType;
        this.messageClazz = messageClazz;
        this.statusMessage = statusMessage;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
