package com.dreamscale.gridtime.core.machine.clock;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GridtimeSequence {
    private long sequenceNumber;
    private GeometryClock.GridTime gridTime;
}
