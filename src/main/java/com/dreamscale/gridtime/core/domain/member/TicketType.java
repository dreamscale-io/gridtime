package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.api.flow.event.EventType;

import java.util.Map;

public enum TicketType {

    INVITE_TO_ORG, INVITE_TO_TEAM, INVITE_TO_ROOM,

    ACTIVATION_BY_OWNER, EMAIL_VALIDATION, ACTIVATE_AND_INVITE_TO_ORG, ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM;

    public static boolean isInviteAndActivateType(TicketType ticketType) {
        return (ticketType == ACTIVATE_AND_INVITE_TO_ORG || ticketType ==  ACTIVATE_AND_INVITE_TO_ORG_AND_TEAM);
    }
}
