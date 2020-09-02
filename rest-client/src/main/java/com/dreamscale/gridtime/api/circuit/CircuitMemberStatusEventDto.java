package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircuitMemberStatusEventDto implements MessageDetailsBody {

    String statusEvent;
    String statusMessage;

    CircuitMemberStatusDto roomMember;
}
