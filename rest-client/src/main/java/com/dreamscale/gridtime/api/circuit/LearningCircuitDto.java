package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitDto {

    UUID id;
    String circuitName;
    String description;
    List<String> tags;

    String wtfTalkRoomName;
    UUID wtfTalkRoomId;

    String retroTalkRoomName;
    UUID retroTalkRoomId;

    private UUID ownerId;
    private String ownerName;

    private UUID moderatorId;
    private String moderatorName;

    LocalDateTime openTime;
    String openTimeStr;

    LocalDateTime closeTime;
    String closeTimeStr;

    String circuitStatus;

    LocalDateTime lastOnHoldTime;
    String lastOnHoldTimeStr;

    LocalDateTime lastResumeTime;
    String lastResumeTimeStr;

    Long secondsBeforeOnHold;

}
