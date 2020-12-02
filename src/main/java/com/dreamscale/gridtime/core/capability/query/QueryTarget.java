package com.dreamscale.gridtime.core.capability.query;

import com.dreamscale.gridtime.api.query.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class QueryTarget {
    private TargetType targetType;
    private UUID organizationId;
    private UUID targetId;
}
