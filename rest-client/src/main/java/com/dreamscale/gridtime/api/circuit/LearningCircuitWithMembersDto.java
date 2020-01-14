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
    UUID moderatorId;

    LocalDateTime openTime;
    LocalDateTime closeTime;

    String circuitStatus;

    LocalDateTime lastOnHoldTime;
    LocalDateTime lastResumeTime;

    Long secondsBeforeOnHold;

    List<CircuitMemberStatusDto> circuitMembers = new ArrayList<>();

    public void addCircuitMember(CircuitMemberStatusDto circuitMemberStatus) {
        circuitMembers.add(circuitMemberStatus);
    }

}
