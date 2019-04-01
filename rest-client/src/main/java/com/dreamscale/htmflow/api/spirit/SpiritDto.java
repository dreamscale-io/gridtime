package com.dreamscale.htmflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpiritDto {

    private UUID spiritId;
    private XPSummaryDto xpSummary;
    private ActiveLinksNetworkDto activeSpiritLinks;

}
