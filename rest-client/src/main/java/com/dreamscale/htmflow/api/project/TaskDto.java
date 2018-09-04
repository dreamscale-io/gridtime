package com.dreamscale.htmflow.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {

    private UUID id;
    private String name;
    private String summary;
    private String status;

    private String externalId;

    private UUID projectId;

}
