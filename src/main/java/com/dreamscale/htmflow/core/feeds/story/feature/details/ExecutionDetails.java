package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@ToString
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

    private Duration durationSinceLastExecution;
    private Duration durationUntilNextExecution;

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
        if (isUnitTest && exitCode != 0) {
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

    public void setDurationSinceLastExecution(Duration durationSinceLastExec) {
        this.durationSinceLastExecution = durationSinceLastExec;
    }

    public void setDurationUntilNextExecution(Duration durationUntilNextExecution) {
        this.durationUntilNextExecution = durationUntilNextExecution;
    }

    public Duration getDurationUntilNextExecution() {
        return durationUntilNextExecution;
    }

    private enum UnitTestType {
        JUnit
    }
}
