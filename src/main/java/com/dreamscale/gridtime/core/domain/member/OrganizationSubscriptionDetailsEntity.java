package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.api.organization.SubscriptionStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "organization_subscription_details_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationSubscriptionDetailsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID rootAccountOwnerId;

    private UUID organizationId;

    private Integer totalSeats;

    private Integer seatsRemaining;

    private Boolean requireMemberEmailInDomain;

    private LocalDateTime creationDate;

    private String organizationName;

    private String domainName;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

}
