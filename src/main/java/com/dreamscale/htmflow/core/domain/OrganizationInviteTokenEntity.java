package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.api.organization.ExternalMemberDto;
import com.dreamscale.htmflow.api.organization.MasterAccountDto;
import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.status.Status;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "organization_invite_token")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationInviteTokenEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String token;

    @Column(name = "organization_id")
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;


    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

}
