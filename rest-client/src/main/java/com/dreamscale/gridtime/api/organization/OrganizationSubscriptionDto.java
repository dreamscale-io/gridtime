package com.dreamscale.gridtime.api.organization;

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
public class OrganizationSubscriptionDto {

    private UUID id;

    private UUID organizationId;
    private String organizationName;
    private String domainName;

    private Integer totalSeats;
    private Integer seatsRemaining;

    private Boolean requireMemberEmailInDomain;

    private LocalDateTime creationDate;

    private SubscriptionStatus subscriptionStatus;
}
