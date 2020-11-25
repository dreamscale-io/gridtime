package com.dreamscale.gridtime.api.terminal;

import com.dreamscale.gridtime.api.circuit.CircuitMemberStatusDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCircuitDto {

    private String circuitName;

    private UUID talkRoomId;

    private LocalDateTime createdDate;

    private CircuitMemberStatusDto creator;

    private List<CircuitMemberStatusDto> circuitMembers;

}
