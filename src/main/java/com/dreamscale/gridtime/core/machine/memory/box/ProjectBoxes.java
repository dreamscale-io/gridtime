package com.dreamscale.gridtime.core.machine.memory.box;

import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcher;
import com.dreamscale.gridtime.core.machine.memory.box.matcher.BoxMatcherConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectBoxes {

    private Map<String, String> packageToBoxMap;
    private List<BoxMatcher> boxMatchers;

    public ProjectBoxes() {
        packageToBoxMap = new HashMap<>();
        boxMatchers = new ArrayList<>();
    }

    public ProjectBoxes(BoxMatcherConfig... boxMatcherConfigs) {
        packageToBoxMap = new HashMap<>();
        boxMatchers = new ArrayList<>();

        for (BoxMatcherConfig config : boxMatcherConfigs) {
            boxMatchers.add(new BoxMatcher(config));
        }
    }

    public ProjectBoxes(List<BoxMatcherConfig> boxMatcherConfigs) {
        packageToBoxMap = new HashMap<>();
        boxMatchers = new ArrayList<>();

        for (BoxMatcherConfig config : boxMatcherConfigs) {
            boxMatchers.add(new BoxMatcher(config));
        }
    }

    public String identifyBox(String filePath) {

        String folderPath = extractFolderPath(filePath);

        String boxName = packageToBoxMap.get(folderPath);

        if (boxName == null) {
            for (BoxMatcher boxMatcher : boxMatchers) {
                if (boxMatcher.matches(filePath)) {
                    boxName = boxMatcher.getBoxName();
                    packageToBoxMap.put(folderPath, boxName);
                    break;
                }
            }
        }

        if (boxName == null) {
            boxName = "default";
        }

        return boxName;
    }

    private String extractFolderPath(String filePath) {
        if (filePath != null) {
            int indexOfLastSlash = filePath.lastIndexOf("/");

            return filePath.subSequence(0, indexOfLastSlash + 1).toString();
        } else {
            return "";
        }
    }

}
