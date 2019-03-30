package com.dreamscale.ideaflow.core.domain;

import com.dreamscale.ideaflow.api.organization.OnlineStatus;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_member_work_status_view")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberWorkStatusEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    private String email;

    private Integer totalXp;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "last_activity")
    private LocalDateTime lastActivity;

    @Column(name = "online_status")
    private OnlineStatus onlineStatus;

    @Column(name = "active_task_id")
    private UUID activeTaskId;

    private String activeTaskName;

    private String activeTaskSummary;

    private String workingOn;

    @Column(name = "active_circle_id")
    private UUID activeCircleId;

}


