package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigInteger;
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
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSequence;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID boxFeatureId;

    private Integer timeInBox;

    private Integer timeInWtf;

    private Integer timeInLearning;

    private Integer timeInProgress;

    private Integer timeInPairing;

    private Float avgFlame;

    private Float avgBatchSize;

    private Float avgTraversalSpeed;

    private Float avgExecutionTime;

    private Float avgRedToGreenTime;


//    create table grid_bridge_metrics (
//            id uuid primary key not null,
//            team_id uuid not null,
//            torchie_id uuid not null,
//            zoom_level text,
//            tile_seq bigint,
//            bridge_feature_id uuid,
//            total_visits int,
//            visits_during_wtf int,
//            visits_during_learning int,
//            avg_flame float,
//            avg_traversal_speed float
//    );
}
