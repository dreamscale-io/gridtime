package com.dreamscale.gridtime.core.hooks.talk.dto;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.journal.JournalEntryDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import lombok.Getter;

@Getter
public enum CircuitMessageType {
    CHAT("chat", null, ChatMessageDetailsDto.class),
    SCREENSHOT("screenshot", null, null),
    SNIPPET("snippet", null, SnippetMessageDetailsDto.class),
    ROOM_MEMBER_JOIN("room-member-join", "Member joined.", RoomMemberStatusEventDto.class),
    ROOM_MEMBER_LEAVE("room-member-inactive", "Member has left.", RoomMemberStatusEventDto.class),
    ROOM_MEMBER_OFFLINE("room-member-offline", "Member is offline.", RoomMemberStatusEventDto.class),
    ROOM_MEMBER_ONLINE("room-member-online", "Member is online.", RoomMemberStatusEventDto.class),
    WTF_STARTED("wtf-started", "WTF is started.", CircuitStatusDto.class),
    WTF_SOLVED("wtf-solved", "WTF is solved.", CircuitStatusDto.class),
    WTF_ONHOLD("wtf-onhold", "WTF is on hold.", CircuitStatusDto.class),
    WTF_RESUMED("wtf-resumed", "WTF is resumed.", CircuitStatusDto.class),
    WTF_RETRO_STARTED("wtf-retro-started", "WTF retro is started.", CircuitStatusDto.class),
    WTF_CANCELED("wtf-canceled", "Circuit is canceled", CircuitStatusDto.class),
    TEAM_INTENTION_STARTED("team-intention-started", "Intention started.", IntentionStartedDetailsDto.class),
    TEAM_INTENTION_ABORTED("team-intention-aborted", "Intention aborted.", IntentionAbortedDetailsDto.class),
    TEAM_INTENTION_FINISHED("team-intention-finished", "Intention finished.", IntentionFinishedDetailsDto.class),
    TEAM_INTENTION_UPDATE("team-intention-updated", "Intention updated.", JournalEntryDto.class),
    TEAM_WTF_STARTED("team-wtf-started", "WTF started.", WTFStartedDto.class),
    TEAM_WTF_STOPPED("team-wtf-stopped", "WTF stopped.", WTFStoppedDto.class),
    TEAM_WTF_JOINED("team-wtf-joined", "WTF joined.", WTFJoinedDto.class),
    TEAM_RETRO_STARTED("team-retro-started", "Retro started.", WTFRetroStartedDto.class),
    TEAM_RETRO_CLOSED("team-retro-stopped", "Retro stopped.", WTFRetroStoppedDto.class),
    TEAM_MEMBER_STATUS_UPDATE("team-member-status-update", "Member status updated.", TeamMemberDto.class),
    TEAM_MEMBER_XP_UPDATE("team-member-xp-update", "Member XP updated.", XPStatusUpdateDto.class);


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

    public Class<?> getMessageClass() {
        return messageClazz;
    }

    public String getStatusMessage() {
        return statusMessage;
    }
}
