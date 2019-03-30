package com.dreamscale.ideaflow.api.organization;

import com.dreamscale.ideaflow.api.spirit.XPSummaryDto;
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
public class TeamMemberWorkStatusDto {
    private UUID id;
    private UUID teamId;

    private String email;
    private String fullName;
    private String shortName;
    private String moodRating;

    private XPSummaryDto xpSummary;

    private LocalDateTime lastActivity;
    private String activeStatus;

    private UUID activeTaskId;
    private String activeTaskName;
    private String activeTaskSummary;
    private String workingOn;

    private String spiritStatus;
    private String spiritMessage;

    private Long alarmDurationInSeconds;
}
