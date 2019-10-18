package com.dreamscale.gridtime.core.machine.memory.box;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;
import org.springframework.util.MultiValueMap;

import java.util.*;

public class TeamBoxConfiguration {

    private Map<UUID, ProjectBoxes> projectBoxesByProjectId = DefaultCollections.map();
    private ProjectBoxes defaultBoxes = new ProjectBoxes();

    private TeamBoxConfiguration() {}

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

    private void addProjectBoxes(UUID projectId, ProjectBoxes projectBoxes) {
        projectBoxesByProjectId.put(projectId, projectBoxes);
    }

    public static class Builder {

        private MultiValueMap<UUID, BoxMatcherConfig> boxMatcherConfigs = DefaultCollections.multiMap();

        public TeamBoxConfiguration build() {
            TeamBoxConfiguration teamBoxConfiguration = new TeamBoxConfiguration();

            for (UUID projectId : boxMatcherConfigs.keySet()) {

                List<BoxMatcherConfig> boxMatchers = boxMatcherConfigs.get(projectId);

                ProjectBoxes projectBoxes = new ProjectBoxes(boxMatchers);
                teamBoxConfiguration.addProjectBoxes(projectId, projectBoxes);
            }

            return teamBoxConfiguration;
        }

        public void boxMatcher(UUID projectId, BoxMatcherConfig boxMatcherConfig) {
            boxMatcherConfigs.add(projectId, boxMatcherConfig);
        }
    }

}
