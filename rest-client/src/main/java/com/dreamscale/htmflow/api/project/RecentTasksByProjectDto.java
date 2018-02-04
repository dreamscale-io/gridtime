package com.dreamscale.htmflow.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentTasksByProjectDto {

    private List<ProjectDto> recentProjects;
    private Map<UUID, List<TaskDto>> recentTasksByProjectId;

}
