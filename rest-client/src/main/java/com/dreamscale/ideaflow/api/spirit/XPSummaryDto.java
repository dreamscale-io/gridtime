package com.dreamscale.ideaflow.api.spirit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XPSummaryDto {

    private int level;
    private int totalXP;

    private int xpProgress;
    private int xpRequiredToLevel;

    private String title;

}
