package com.dreamscale.gridtime.core.hooks.talk.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircuitStatusMessageDto {

    UUID circuitId;
    String circuitName;
    String statusType;
    String message;
}
