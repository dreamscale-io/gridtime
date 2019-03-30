package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.time.Duration;

public class LocationInThought implements IdeaFlowFeature {

    private final FocalPoint mainFocus;
    private final String locationPath;
    private final int locationIndex;

    private int modificationCount;
    private Duration totalTimeInvestment;
    private int visitCounter;

    LocationInThought(FocalPoint mainFocus, String locationPath, int locationIndex) {
        this.mainFocus = mainFocus;
        this.locationPath = locationPath;
        this.totalTimeInvestment = Duration.ofSeconds(0);
        this.visitCounter = 0;
        this.modificationCount = 0;
        this.locationIndex = locationIndex;
    }

    public String getLocationPath() {
        return locationPath;
    }

    void visit() {
        this.visitCounter++;
    }

    public void spendTime(Duration additionalTime) {
        this.totalTimeInvestment = totalTimeInvestment.plus(additionalTime);
    }

    public void modify(int modificationCount) {
        this.modificationCount += modificationCount;
    }

    public String getMainFocusName() {
        return mainFocus.getName();
    }

    public String toKey() {
       return "["+ mainFocus.getName() + "]:"+locationPath;
    }

    public Duration getTotalTimeInvestment() {
        return totalTimeInvestment;
    }
}
