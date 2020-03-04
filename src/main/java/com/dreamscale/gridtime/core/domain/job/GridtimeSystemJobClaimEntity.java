package com.dreamscale.gridtime.core.domain.job;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "gridtime_system_job_claim")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridtimeSystemJobClaimEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private SystemJobType jobType;

    private String jobDescriptorJson;

    private LocalDateTime startedOn;

    private LocalDateTime finishedOn;

    private LocalDateTime lastHeartbeat;

    private String errorMessage;

    @Enumerated(EnumType.STRING)
    private JobStatusType jobStatus;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID claimingWorkerId;

}

