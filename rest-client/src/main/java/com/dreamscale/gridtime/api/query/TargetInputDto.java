package com.dreamscale.gridtime.api.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetInputDto {

    TargetType targetType;
    String targetName;
}

