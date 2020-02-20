package com.dreamscale.gridtime.core.domain.job;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "gridtime_system_job")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridtimeSystemJobEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID jobId;

    private String jobName;

    private SystemJobType jobType;

    private String jobConfigJson;

    LocalDateTime startedOn;

    LocalDateTime lastHeartbeat;

    @Enumerated(EnumType.STRING)
    private JobExitStatus lastExitStatus;

    @Enumerated(EnumType.STRING)
    private RunStatus runStatus;

    private UUID claimingWorkerId;

}
