package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.FinishTag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class FinishCircleTag implements FinishTag {
    private UUID circleId;
    private String circleName;
    private String finishType;

}
