package com.dreamscale.gridtime.api.flow.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewExecutionActivityDto implements Activity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

    private String processName;
    private int exitCode;
    private String executionTaskType;
    private boolean isDebug;

}
