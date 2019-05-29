package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.types;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CycleMetricTag implements Tag {
    LocalDateTime position;

    Duration durationSinceLastEvent;

    @Override
    public String toDisplayString() {
        return durationSinceLastEvent.toString();
    }
}
