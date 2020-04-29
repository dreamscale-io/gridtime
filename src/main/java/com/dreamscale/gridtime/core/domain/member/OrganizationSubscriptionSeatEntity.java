package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.api.organization.SubscriptionStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "organization_subscription_seat")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSubscriptionSeatEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID subscriptionId;

    private UUID organizationId;

    private UUID rootAccountId;

    private String orgEmail;

    private LocalDateTime activationDate;

    private LocalDateTime cancelDate;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

}

