package com.dreamscale.htmflow.core.hooks.hypercore;

import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.Map;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface HypercoreClient {

    @RequestLine("POST "+ HypercorePaths.HYPERCORE_PATH+ HypercorePaths.CREATE_PATH)
    HypercoreDto create();

    @RequestLine("POST "+ HypercorePaths.HYPERCORE_PATH + "/{key}" + HypercorePaths.APPEND_PATH)
    AppendResponseDto append(@Param("key") String id, Map<String, String> propertyMap);


}
