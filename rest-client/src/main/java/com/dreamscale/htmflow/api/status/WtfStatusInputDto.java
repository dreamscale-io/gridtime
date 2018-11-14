package com.dreamscale.htmflow.api.status;

import com.dreamscale.htmflow.api.status.XPSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WtfStatusInputDto {
    private String problemStatement;

    //TODO add screenshot

}
