package com.dreamscale.gridtime.core.domain.tile.metrics;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "grid_box_metrics")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridBoxMetricsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSequence;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID boxFeatureId;

    private Long timeInBox;

    private Double percentWtf;

    private Double percentLearning;

    private Double percentProgress;

    private Double percentPairing;

    private Double avgFlame;

    private Double avgFileBatchSize;

    private Double avgTraversalSpeed;

    private Double avgExecutionTime;

}
