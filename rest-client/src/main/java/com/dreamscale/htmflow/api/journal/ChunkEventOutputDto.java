package com.dreamscale.htmflow.api.journal;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
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
public class ChunkEventOutputDto {

    private UUID id;
    private LocalDateTime position;
    private String description;
    private UUID projectId;
    private UUID taskId;
}
