package com.dreamscale.gridtime.core.hooks.talk.dto;

import lombok.Getter;

@Getter
public enum TalkMessageType {
    CHAT("chat", null, ChatMessageDto.class),
    SCREENSHOT("screenshot", null, null),
    SNIPPET("snippet", null, null),
    ROOM_MEMBER_JOIN("room-member-join", "Member joined.", null),
    ROOM_MEMBER_INACTIVE("room-member-inactive", "Member inactive.", null),
    ROOM_MEMBER_ACTIVE("room-member-active", "Member is active.", null),
    CIRCUIT_OPEN("circuit-open", "Circuit is opened.",CircuitStatusMessageDto.class),
    CIRCUIT_CLOSED("circuit-closed", "Circuit is closed.", CircuitStatusMessageDto.class),
    CIRCUIT_ONHOLD("circuit-onhold", "Circuit is on hold.", CircuitStatusMessageDto.class),
    CIRCUIT_RESUMED("circuit-resume", "Circuit is resumed.", CircuitStatusMessageDto.class),
    CIRCUIT_RETRO_STARTED("circuit-retro", "Circuit retro is started.", CircuitStatusMessageDto.class);

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
