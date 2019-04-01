package com.dreamscale.htmflow.api.circle;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateWTFCircleInputDto {

    String problemDescription;
}
