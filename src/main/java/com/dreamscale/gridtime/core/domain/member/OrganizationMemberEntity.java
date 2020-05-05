package com.dreamscale.gridtime.core.domain.member;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "organization_member")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationMemberEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String email;

    private String username;

    private String lowerCaseUserName;

    private LocalDateTime lastUpdated;

    @Column(name = "root_account_id")
    private UUID rootAccountId;

    @Column(name = "external_id")
    private String externalId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

}
