package com.dreamscale.htmflow.core.feeds.story.feature.details;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class ExecutionDetails extends Details {

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


    public ExecutionDetails(LocalDateTime moment, Duration duration) {
        this();
        this.position = moment;
        this.duration = duration;
    }

    public ExecutionDetails() {
        super(DetailsType.EXECUTION);
    }

    public void setExecutionTaskType(String executionTaskType) {
        this.executionTaskType = executionTaskType;
        this.isUnitTest = evalIsUnitTest();
    }

    public void setRed(boolean red) {}
    public void setGreen(boolean green) {}

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

    private enum UnitTestType {
        JUnit
    }
}
