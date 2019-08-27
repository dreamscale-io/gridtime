package com.dreamscale.gridtime.core.domain.member;

import com.dreamscale.gridtime.api.organization.OnlineStatus;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "member_status_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberStatusEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "online_status")
    private OnlineStatus onlineStatus;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @Column(name = "active_task_id")
    private UUID activeTaskId;

    private String activeTaskName;

    private String activeTaskSummary;

    private String workingOn;

    @Column(name = "active_circle_id")
    private UUID activeCircleId;

    private Integer totalXp;

}
