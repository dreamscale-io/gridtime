package com.dreamscale.gridtime.core.capability.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class QueryTimeScope {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
