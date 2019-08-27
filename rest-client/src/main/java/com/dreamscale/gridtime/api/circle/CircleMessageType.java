package com.dreamscale.gridtime.api.circle;

public enum CircleMessageType {
    PROBLEM_STATEMENT, STATUS_UPDATE, //deprecated, but still in DB
    CIRCLE_START, CIRCLE_CLOSED, CIRCLE_SHELVED, CIRCLE_RESUMED, CHAT, SCREENSHOT, SNIPPET;
}
