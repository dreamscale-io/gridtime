package com.dreamscale.gridtime.core.domain.work;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "torchie_feed_cursor")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class TorchieFeedCursorEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    private LocalDateTime lastPublishedDataCursor;

    private LocalDateTime lastTileProcessedCursor;

    private LocalDateTime nextWaitUntilCursor;

    private LocalDateTime firstTilePosition;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID claimingServerId;

    private LocalDateTime lastClaimUpdate;

    private Integer failureCount;

    @Enumerated(EnumType.STRING)
    private ClaimType claimType;
}

