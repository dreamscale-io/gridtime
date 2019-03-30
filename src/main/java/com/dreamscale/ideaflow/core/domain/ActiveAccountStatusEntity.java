package com.dreamscale.ideaflow.core.domain;

import com.dreamscale.ideaflow.api.organization.OnlineStatus;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "active_account_status")
@Data
@EqualsAndHashCode(of = "masterAccountId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveAccountStatusEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "master_account_id")
    private UUID masterAccountId;

    @Column(name = "connection_id")
    private UUID connectionId;

    @Column(name = "delta_time")
    private Integer deltaTime;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "online_status")
    private OnlineStatus onlineStatus;

}
