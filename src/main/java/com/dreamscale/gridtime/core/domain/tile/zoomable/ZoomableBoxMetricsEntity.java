package com.dreamscale.gridtime.core.domain.tile.zoomable;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "zoomable_idea_flow_metrics_v")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ZoomableBoxMetricsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSeq;

    private String gridTime;

    private LocalDateTime clockTime;

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
