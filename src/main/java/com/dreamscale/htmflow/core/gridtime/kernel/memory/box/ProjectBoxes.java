package com.dreamscale.htmflow.core.gridtime.kernel.memory.box;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.matcher.BoxMatcher;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.box.matcher.BoxMatcherConfig;

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

    public void configureBoxMatcher(BoxMatcherConfig boxMatcherConfig) {
        boxMatchers.add(new BoxMatcher(boxMatcherConfig));
    }

}
