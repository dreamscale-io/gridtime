package com.dreamscale.htmflow.core.gridtime.machine.memory.box;

import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.box.matcher.BoxMatcherConfig;

import java.util.Map;
import java.util.UUID;

public class TeamBoxConfiguration {

    private Map<UUID, ProjectBoxes> projectBoxesByProjectId = DefaultCollections.map();
    private ProjectBoxes defaultBoxes = new ProjectBoxes();

    public void configureMatcher(UUID projectId, BoxMatcherConfig boxMatcherConfig) {
        ProjectBoxes projectBoxes = findOrCreateProjectBoxes(projectId);
        projectBoxes.configureBoxMatcher(boxMatcherConfig);
    }

    public String identifyBox(UUID projectId, String filePath) {
        if (projectId != null) {
            ProjectBoxes projectBoxes = findOrCreateProjectBoxes(projectId);
            return projectBoxes.identifyBox(filePath);
        } else {
            return defaultBoxes.identifyBox(filePath);
        }
    }

    private ProjectBoxes findOrCreateProjectBoxes(UUID projectId) {
        ProjectBoxes projectBoxes = projectBoxesByProjectId.get(projectId);
        if (projectBoxes == null) {
            projectBoxes = new ProjectBoxes();
            projectBoxesByProjectId.put(projectId, projectBoxes);
        }
        return projectBoxes;
    }



}
