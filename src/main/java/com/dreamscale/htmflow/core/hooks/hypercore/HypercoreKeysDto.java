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
public class HypercoreKeysDto {

    private Map<String, String> keys = new HashMap<>();

    public String getDiscoveryKey() {
        return keys.get("discoveryKey");
    }

    public String getSecretKey() {
        return keys.get("secretKey");
    }

    public String getKey() {
        return keys.get("key");
    }

}

