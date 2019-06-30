package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;


@Getter
@Setter
@ToString
public class ExecutionEvent implements FeatureDetails {

    private Long executionId;
    private LocalDateTime position;
    private Duration duration;
    private String processName;
    private String executionTaskType;
    private int exitCode;
    private boolean isDebug;
    private boolean isUnitTest;


    public ExecutionEvent(Long executionId, LocalDateTime moment, Duration duration) {
        this.executionId = executionId;
        this.position = moment;
        this.duration = duration;
    }

    public ExecutionEvent(Long executionId, LocalDateTime moment, Duration duration, String executionTaskType, int exitCode) {
        this.executionId = executionId;
        this.position = moment;
        this.duration = duration;
        setExecutionTaskType(executionTaskType);
        setExitCode(exitCode);
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
    public boolean isUnitTest() {
        return isUnitTest;
    }

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createExecutionKey(isUnitTest(), executionId);
    }

    private enum UnitTestType {
        JUnit
    }
}
