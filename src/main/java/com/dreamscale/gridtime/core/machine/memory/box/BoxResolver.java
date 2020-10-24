package com.dreamscale.gridtime.core.machine.memory.box;

import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.LRUMap;

import java.util.*;

@Slf4j
public class BoxResolver {

    private final BoxConfigLoader boxConfigLoader;

    private static final int PROJECT_CACHE_SIZE = 100;

    private LRUMap boxConfigsByProject = new LRUMap(PROJECT_CACHE_SIZE);

    private ProjectBoxes defaultBoxes = new ProjectBoxes();

    public BoxResolver(BoxConfigLoader boxConfigLoader) {
        this.boxConfigLoader = boxConfigLoader;
    }

    public String identifyBox(UUID projectId, String filePath) {
        if (projectId != null) {
            ProjectBoxes projectBoxes = findOrLoadProjectBoxes(projectId);
            return projectBoxes.identifyBox(filePath);
        } else {
            return defaultBoxes.identifyBox(filePath);
        }
    }

    private ProjectBoxes findOrLoadProjectBoxes(UUID projectId) {
        ProjectBoxes projectBoxes = (ProjectBoxes) boxConfigsByProject.get(projectId);
        if (projectBoxes == null) {
            ProjectBoxes loadedFromDB = null;
            if (boxConfigLoader != null) {
                loadedFromDB = boxConfigLoader.loadProjectBoxesFromDB(projectId);
            }
            if (loadedFromDB != null) {
                boxConfigsByProject.put(projectId, loadedFromDB);
            } else {
                projectBoxes = defaultBoxes;
            }
        }
        return projectBoxes;
    }

    public void addBoxConfig(UUID projectId, BoxMatcherConfig boxMatcherConfig) {
        ProjectBoxes projectBoxes = (ProjectBoxes)boxConfigsByProject.get(projectId);

        if (projectBoxes == null) {
            projectBoxes = new ProjectBoxes();
            boxConfigsByProject.put(projectId, projectBoxes);
        }

        projectBoxes.addBoxMatcher(boxMatcherConfig);
    }


}
