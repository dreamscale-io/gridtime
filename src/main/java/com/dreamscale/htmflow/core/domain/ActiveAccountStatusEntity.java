package com.dreamscale.htmflow.core.domain;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "active_account_status")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveAccountStatusEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    @Column(name = "master_account_id")
    private UUID masterAccountId;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "active_status")
    private ActiveAccountStatus activeStatus;

}
