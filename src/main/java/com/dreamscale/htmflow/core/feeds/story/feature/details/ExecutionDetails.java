package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;
import java.time.LocalDateTime;

public class ExecutionDetails extends FlowFeature {

    private LocalDateTime position;
    private Duration duration;
    private String processName;
    private String executionTaskType;
    private int exitCode;
    private boolean isDebug;
    private boolean isUnitTest;
    private boolean isFirstRed;
    private boolean isEndOfReds;
    private boolean isRedAndWantingGreen;

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public void setExecutionTaskType(String executionTaskType) {
        this.executionTaskType = executionTaskType;
        this.isUnitTest = evalIsUnitTest();
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public void setIsDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public boolean isRed() {
        boolean isRed = false;
        if (isUnitTest && exitCode > 0) {
            isRed = true;
        }
        return isRed;
    }

    public boolean isGreen() {
        boolean isGreen = false;
        if (isUnitTest && exitCode == 0) {
            isGreen = true;
        }
        return isGreen;
    }

    private boolean evalIsUnitTest() {
        boolean isUnitTest = false;
        for ( UnitTestType type : UnitTestType.values()) {
            if (type.name().equals(executionTaskType) ) {
                isUnitTest = true;
                break;
            }
        }
        return isUnitTest;
    }

    public void setFirstRed(boolean isFirstRed) {
        this.isFirstRed = isFirstRed;
    }

    public void setEndOfReds(boolean isEndOfReds) {
        this.isEndOfReds = isEndOfReds;
    }

    public boolean isUnitTest() {
        return isUnitTest;
    }

    public boolean isRedAndWantingGreen() {
        return isRedAndWantingGreen;
    }

    public void setIsRedAndWantingGreen(boolean isRedAndWantingGreen) {
        this.isRedAndWantingGreen = isRedAndWantingGreen;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public void setPosition(LocalDateTime position) {
        this.position = position;
    }

    public LocalDateTime getPosition() {
        return position;
    }

    private enum UnitTestType {
        JUnit
    }
}
