package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.api.organization.SubscriptionStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "organization_subscription")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSubscriptionEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID rootAccountOwnerId;

    private UUID organizationId;

    private Integer totalSeats;

    private Integer seatsRemaining;

    private Boolean requireMemberEmailInDomain;

    private String stripePaymentId;

    private String stripeCustomerId;

    private LocalDateTime creationDate;

    private LocalDateTime lastModifiedDate;

    private LocalDateTime lastStatusCheck;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

}
