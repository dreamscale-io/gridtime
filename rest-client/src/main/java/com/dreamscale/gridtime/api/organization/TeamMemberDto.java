package com.dreamscale.gridtime.api.organization;

import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.circuit.MessageDetailsBody;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberDto implements MessageDetailsBody {
    private UUID id;

    private String email;
    private String fullName;
    private String displayName;

    private String username;

    private XPSummaryDto xpSummary;

    private CircuitJoinType activeJoinType;
    private LearningCircuitDto activeCircuit;
    private OnlineStatus onlineStatus;

    private UUID activeTaskId;
    private String activeTaskName;
    private String activeTaskSummary;
    private String workingOn;

}
