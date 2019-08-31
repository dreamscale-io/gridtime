package com.dreamscale.gridtime.core.domain.circle;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "circle")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID ownerMemberId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID channelId;

    private String circleName;

    private String problemDescription;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private boolean onShelf;
    private LocalDateTime lastShelfTime;
    private LocalDateTime lastResumeTime;


    private Long durationInSeconds;



}