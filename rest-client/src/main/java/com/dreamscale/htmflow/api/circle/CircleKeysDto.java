package com.dreamscale.htmflow.api.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleKeyDto {

    UUID circleId;
    private String hypercoreFeedId;
    private String hypercorePublicKey;
    private String hypercoreSecretKey;
}
