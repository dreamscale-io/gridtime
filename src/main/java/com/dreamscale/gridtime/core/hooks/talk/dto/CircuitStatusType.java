package com.dreamscale.gridtime.core.hooks.talk.dto;

import lombok.Getter;

@Getter
public enum CircuitStatusType {
    CIRCUIT_OPEN("circuit-open", "Circuit is opened.", CircuitStatusMessageDto.class),
    CIRCUIT_CLOSED("circuit-closed", "Circuit is closed.", CircuitStatusMessageDto.class),
    CIRCUIT_ONHOLD("circuit-onhold", "Circuit is on hold.", CircuitStatusMessageDto.class),
    CIRCUIT_RESUME("circuit-resume", "Circuit is resumed.", CircuitStatusMessageDto.class),
    CIRCUIT_RETRO("circuit-retro", "Circuit retro is started.", CircuitStatusMessageDto.class);

    private final String talkMessageType;
    private final Class<?> messageClazz;
    private final String statusMessage;

    CircuitStatusType(String talkMessageType, String statusMessage, Class<?> messageClazz) {
        this.talkMessageType = talkMessageType;
        this.statusMessage = statusMessage;
        this.messageClazz = messageClazz;
    }

    public String getMessage() {
        return statusMessage;
    }
}
