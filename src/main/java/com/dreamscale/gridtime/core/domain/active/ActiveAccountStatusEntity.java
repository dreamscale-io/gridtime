package com.dreamscale.gridtime.core.domain.active;

import com.dreamscale.gridtime.api.organization.OnlineStatus;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
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

    @Column(name = "delta_time")
    private Integer deltaTime;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "online_status")
    private OnlineStatus onlineStatus;

}
