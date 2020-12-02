package com.dreamscale.gridtime.api.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryInputDto {

    TimeScope timeScope;
    String gridtimeScopeExpression; //gt[2019,3, 1-3]

    TargetType targetType;
    String targetName;

    Integer limit;

    public QueryInputDto(TimeScope timeScope) {
        this.timeScope = timeScope;
    }

    public QueryInputDto(TimeScope timeScope, TargetType targetType, String targetName) {
        this.timeScope = timeScope;
        this.targetType = targetType;
        this.targetName = targetName;
    }

    public QueryInputDto(TimeScope timeScope, int limit) {
        this.timeScope = timeScope;
        this.limit = limit;
    }

    public QueryInputDto(TimeScope timeScope, TargetType targetType, String targetName, int limit) {
        this.timeScope = timeScope;
        this.targetType = targetType;
        this.targetName = targetName;
        this.limit = limit;
    }

}

