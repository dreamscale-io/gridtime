package com.dreamscale.gridtime.core.domain.job;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "gridtime_job")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridtimeJobEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID jobId;

    private String jobName;

    private JobType jobType;

    private UUID ownerId;

    private JobOwnerType jobOwnerType;

    private String jobConfigJson;

    LocalDateTime startedOn;

    LocalDateTime lastHeartbeat;

    @Enumerated(EnumType.STRING)
    private JobExitStatus lastExitStatus;

    @Enumerated(EnumType.STRING)
    private RunStatus runStatus;

    private UUID claimingWorkerId;

}
