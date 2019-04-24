package com.dreamscale.htmflow.core.hooks.hypercore;

import java.util.Map;

public class HypercoreConnection {

    private final HypercoreClient hypercoreClient;

    public HypercoreConnection(HypercoreClient hypercoreClient) {
        this.hypercoreClient = hypercoreClient;
    }

    public HypercoreDto create() {
        return hypercoreClient.create();
    }

    public AppendResponseDto append(String key, Map<String, String> propertyMap) {
        return hypercoreClient.append(key, propertyMap);
    }

}
