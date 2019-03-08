package com.dreamscale.htmflow.api.status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WtfStatusInputDto {
    private String problemStatement;

    //TODO add screenshot

}
