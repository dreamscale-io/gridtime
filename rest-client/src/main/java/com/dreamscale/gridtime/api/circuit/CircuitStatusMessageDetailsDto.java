package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircuitStatusMessageDetailsDto implements MessageDetailsBody {

    UUID circuitId;
    String circuitName;
    String statusType;
    String message;
}
