package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitWithMembersDto {

    UUID id;
    String circuitName;

    UUID wtfTalkRoomId;
    String wtfTalkRoomName;

    UUID retroTalkRoomId;
    String retroTalkRoomName;

    UUID ownerId;
    String ownerName;

    UUID moderatorId;
    String moderatorName;

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

    List<CircuitMemberStatusDto> circuitMembers = new ArrayList<>();

    public void addCircuitMember(CircuitMemberStatusDto circuitMemberStatus) {
        circuitMembers.add(circuitMemberStatus);
    }

}
