package com.dreamscale.htmflow.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {

    private String id;
    private String name;
    private String summary;
    private String externalId;

}
