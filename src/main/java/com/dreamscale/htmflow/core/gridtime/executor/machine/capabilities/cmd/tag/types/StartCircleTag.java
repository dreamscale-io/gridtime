package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.StartTag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class StartCircleTag implements StartTag {
    private UUID circleId;
    private String circleName;
    private String startType;


}
