package com.dreamscale.htmflow.api.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewExecutionActivity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

    private String processName;
    private int exitCode;
    private String executionTaskType;
    private boolean isDebug;

}
