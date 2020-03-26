package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LearningCircuitWithMembersDto extends LearningCircuitDto {


    List<CircuitMemberStatusDto> circuitParticipants;

    List<CircuitMemberStatusDto> activeWtfRoomMembers;

    List<CircuitMemberStatusDto> activeRetroRoomMembers;


    public void addCircuitParticipant(CircuitMemberStatusDto circuitMemberStatus) {
        if (circuitParticipants == null) {
            circuitParticipants = new ArrayList<>();
        }

        circuitParticipants.add(circuitMemberStatus);
    }

    public void addActiveWTFRoomMember(CircuitMemberStatusDto circuitMemberStatus) {
        if (activeWtfRoomMembers == null) {
            activeWtfRoomMembers = new ArrayList<>();
        }

        activeWtfRoomMembers.add(circuitMemberStatus);
    }

    public void addActiveRetroRoomMember(CircuitMemberStatusDto circuitMemberStatus) {
        if (activeRetroRoomMembers == null) {
            activeRetroRoomMembers = new ArrayList<>();
        }

        activeRetroRoomMembers.add(circuitMemberStatus);
    }

}
