package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
