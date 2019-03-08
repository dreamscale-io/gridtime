package com.dreamscale.htmflow.api.spirit;

import com.dreamscale.htmflow.api.circle.CircleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpiritNetworkDto {

    UUID spiritId;
    ActiveLinksNetworkDto activeLinksNetwork;
    List<CircleDto> activeCircles;

}
