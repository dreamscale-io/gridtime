package com.dreamscale.htmflow.api.organization;

import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.spirit.XPSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberWorkStatusDto {
    private UUID id;

    private String email;
    private String fullName;
    private String shortName;

    private XPSummaryDto xpSummary;
    private CircleDto activeCircle;
    private OnlineStatus onlineStatus;

    private UUID activeTaskId;
    private String activeTaskName;
    private String activeTaskSummary;
    private String workingOn;

}
