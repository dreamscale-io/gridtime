package com.dreamscale.gridtime.core.domain.active;

import com.dreamscale.gridtime.api.organization.OnlineStatus;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "active_account_status")
@Data
@EqualsAndHashCode(of = "rootAccountId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveAccountStatusEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "root_account_id")
    private UUID rootAccountId;

    @Column(name = "connection_id")
    private UUID connectionId;

    @Column(name = "logged_in_organization_id")
    private UUID loggedInOrganizationId;

    @Column(name = "delta_time")
    private Integer deltaTime;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "online_status")
    @Enumerated(EnumType.STRING)
    private OnlineStatus onlineStatus;

}
