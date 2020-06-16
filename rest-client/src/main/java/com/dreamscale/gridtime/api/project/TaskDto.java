package com.dreamscale.gridtime.api.project;

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

    @Deprecated
    private String summary;

    private String description;

    private String externalId;

    private UUID projectId;

    public void setDescription(String description) {
        this.description = description;
        setSummary(description);
    }

}
