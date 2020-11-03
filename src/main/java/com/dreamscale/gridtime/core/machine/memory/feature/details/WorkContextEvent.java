package com.dreamscale.gridtime.core.machine.memory.feature.details;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class WorkContextEvent {

    private UUID intentionId;
    private String description;

    private UUID taskId;
    private String taskName;

    private UUID projectId;
    private String projectName;

}
