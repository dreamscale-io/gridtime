package com.dreamscale.gridtime.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionCancelRequestDto {

    private UUID organizationId;
}
