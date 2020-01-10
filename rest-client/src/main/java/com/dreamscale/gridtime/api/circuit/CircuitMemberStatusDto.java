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
public class CircuitMemberStatusDto {

    UUID memberId;

    String shortName;
    String fullName;

    LocalDateTime wtfJoinTime;
    String wtfRoomStatus;

    LocalDateTime retroJoinTime;
    String retroRoomStatus;

}
