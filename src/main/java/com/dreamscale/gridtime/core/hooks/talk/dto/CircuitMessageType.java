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
    ROOM_MEMBER_LEAVE("room-member-leave", "Member has left.", RoomMemberStatusEventDto.class),
    ROOM_MEMBER_STATUS_UPDATE("room-member-status-update", "Member status updated.", RoomMemberStatusEventDto.class),


    TEAM_INTENTION_STARTED("team-intention-started", "Intention started.", IntentionStartedDetailsDto.class),
    TEAM_INTENTION_ABORTED("team-intention-aborted", "Intention aborted.", IntentionAbortedDetailsDto.class),
    TEAM_INTENTION_FINISHED("team-intention-finished", "Intention finished.", IntentionFinishedDetailsDto.class),
    TEAM_INTENTION_UPDATE("team-intention-updated", "Intention updated.", JournalEntryDto.class),

    TEAM_WTF_STARTED("team-wtf-started", "WTF started.", WTFStatusUpdateDto.class),
    TEAM_WTF_CANCELED("team-wtf-canceled", "WTF canceled.", WTFStatusUpdateDto.class),
    TEAM_WTF_ON_HOLD("team-wtf-on-hold", "WTF on hold.", WTFStatusUpdateDto.class),
    TEAM_WTF_RESUMED("team-wtf-resumed", "WTF resumed.", WTFStatusUpdateDto.class),
    TEAM_WTF_SOLVED("team-wtf-solved", "WTF solved.", WTFStatusUpdateDto.class),
    TEAM_WTF_CLOSED("team-wtf-closed", "WTF closed.", WTFStatusUpdateDto.class),
    TEAM_WTF_JOINED("team-wtf-joined", "WTF joined.", WTFStatusUpdateDto.class),
    TEAM_RETRO_STARTED("team-wtf-retro-started", "Retro started.", WTFStatusUpdateDto.class),
    TEAM_RETRO_CLOSED("team-retro-closed", "Retro closed.", WTFStatusUpdateDto.class),

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
