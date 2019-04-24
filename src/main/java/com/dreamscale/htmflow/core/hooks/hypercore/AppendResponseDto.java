package com.dreamscale.htmflow.core.hooks.hypercore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AppendResponseDto {

    private String action;
    private String status;
    private Long timestamp;
    private LinkedHashMap message;

    private HypercoreFeedStatusDto response;

}
