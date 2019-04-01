package com.dreamscale.htmflow.api.journal;

import com.dreamscale.htmflow.api.project.ProjectDto;
import com.dreamscale.htmflow.api.project.TaskDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentJournalDto {

    private List<JournalEntryDto> recentIntentions;

    private List<ProjectDto> recentProjects;
    private Map<UUID, List<TaskDto>> recentTasksByProjectId;

    public void addRecentProjectTasks(ProjectDto projectDto, List<TaskDto> tasksForProject) {
        initIfNull();

        recentProjects.add(projectDto);
        recentTasksByProjectId.put(projectDto.getId(), tasksForProject);
    }

    public List<TaskDto> getRecentTasks(UUID projectId) {
        return recentTasksByProjectId.get(projectId);
    }


    private void initIfNull() {
        if (recentProjects == null) {
            recentProjects = new ArrayList<>();
        }
        if (recentTasksByProjectId == null) {
            recentTasksByProjectId = new LinkedHashMap<>();
        }
    }
}
