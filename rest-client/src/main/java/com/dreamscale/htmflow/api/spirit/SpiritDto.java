package com.dreamscale.htmflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpiritDto {

    private XPSummaryDto xpSummary;
    private ActiveLinksNetworkDto activeSpiritLinks;

}
