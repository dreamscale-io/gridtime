package com.dreamscale.gridtime.api.grid;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Feature {
    private UUID id;
    private String featureType;
    private String featureGlyph;
    private String description;

    private String firstReference;
}
