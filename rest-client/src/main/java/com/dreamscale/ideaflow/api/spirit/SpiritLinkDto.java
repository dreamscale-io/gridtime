package com.dreamscale.ideaflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpiritLinkDto {


    private UUID spiritId;
    private UUID friendSpiritId;
    private String name;
}