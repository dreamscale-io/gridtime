package com.dreamscale.gridtime.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionInputDto {

    private String organizationName;
    private String domainName;

    private Boolean requireMemberEmailInDomain;
    private Integer seats;

    private String stripePaymentId;
}
