package com.dreamscale.htmflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TombstoneInputDto {
    String epitaph;
}
