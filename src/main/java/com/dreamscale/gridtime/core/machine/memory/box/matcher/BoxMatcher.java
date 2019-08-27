package com.dreamscale.gridtime.core.machine.memory.box.matcher;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class BoxMatcher {

    private final BoxMatcherConfig boxMatcherConfig;
    private final String boxName;
    private final String includeRegex;
    private final List<String> excludeRegex ;

    public BoxMatcher(BoxMatcherConfig boxMatcherConfig) {
            this.boxMatcherConfig = boxMatcherConfig;

            this.boxName = boxMatcherConfig.getBox();
            this.includeRegex = toRegex(boxMatcherConfig.getInclude());

            if (boxMatcherConfig.getExcludeList() != null && boxMatcherConfig.getExcludeList().size() > 0) {
                List<String> excludeRegex = new ArrayList<>();
                for (String exclusionMatcher : boxMatcherConfig.getExcludeList()) {
                    excludeRegex.add(toRegex(exclusionMatcher));
                }
                this.excludeRegex = excludeRegex;
            } else {
                this.excludeRegex = DefaultCollections.emptyList();
            }
    }

    private String toRegex(String patternMatcherWithStars) {
        return patternMatcherWithStars.replace("*", "\\S*");
    }


    public boolean matches(String pathInsideBox) {

        boolean boxMatches = false;

        if (pathInsideBox != null ) {

            boxMatches = pathInsideBox.matches(includeRegex);

            for (String exclusionRegex : excludeRegex) {
                if (pathInsideBox.matches(exclusionRegex)) {
                    boxMatches = false;
                }
            }

        }
        return boxMatches;
    }

}
