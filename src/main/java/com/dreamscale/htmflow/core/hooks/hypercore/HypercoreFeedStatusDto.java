package com.dreamscale.htmflow.core.hooks.hypercore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HypercoreFeedStatusDto {

    private Integer sequence;
    private Integer hypercoreLength;
    private Integer byteLength;
}
