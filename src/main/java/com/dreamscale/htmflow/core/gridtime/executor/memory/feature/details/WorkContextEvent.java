package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
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
