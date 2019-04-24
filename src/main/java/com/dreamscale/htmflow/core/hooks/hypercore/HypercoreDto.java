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
public class HypercoreDto {

    private String action;
    private String status;
    private Long timestamp;
    private String message;

    private HypercoreKeysDto response;

}
