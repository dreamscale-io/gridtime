package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitDto {

    UUID id;
    String circuitName;

    String wtfTalkRoomName;
    UUID wtfTalkRoomId;

    String retroTalkRoomName;
    UUID retroTalkRoomId;

    UUID ownerId;
    UUID moderatorId;

    LocalDateTime openTime;
    LocalDateTime closeTime;

    String circuitStatus;

    LocalDateTime lastOnHoldTime;
    LocalDateTime lastResumeTime;

    Long secondsBeforeOnHold;

}
