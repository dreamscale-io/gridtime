package com.dreamscale.gridtime.api.circuit;

import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class XPStatusUpdateDto implements MessageDetailsBody {

    String username;
    UUID memberId;

    XPSummaryDto oldXPSummary;
    XPSummaryDto newXPSummary;

}
