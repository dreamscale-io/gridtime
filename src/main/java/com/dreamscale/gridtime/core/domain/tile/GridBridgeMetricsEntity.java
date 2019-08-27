package com.dreamscale.gridtime.core.domain.tile;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "grid_bridge_metrics")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridBridgeMetricsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSequence;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID bridgeFeatureId;

    private Integer totalVisits;

    private Integer visitsDuringWtf;

    private Integer visitsDuringLearning;

    private Integer visitsDuringRedToGreen;

    private Float avgFlame;

    private Float avgTraversalSpeed;

}
