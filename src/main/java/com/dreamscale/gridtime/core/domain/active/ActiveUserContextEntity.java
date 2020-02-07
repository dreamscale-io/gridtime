package com.dreamscale.gridtime.core.domain.active;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "active_user_context")
@Data
@EqualsAndHashCode(of = "rootAccountId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveUserContextEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "root_account_id")
    private UUID rootAccountId;

    private UUID organizationId;

    private UUID memberId;

    private UUID teamId;

}
