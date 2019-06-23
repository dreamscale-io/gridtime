package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigInteger;
import java.util.UUID;

@Entity(name = "grid_tile_summary")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTileSummaryEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private BigInteger tileSequence;

    private Integer timeInTile;

    private Integer timeInWtf;

    private Integer timeInLearning;

    private Integer timeInProgress;

    private Integer timeInPairing;

    private Float avgFlame;

    private Float avgBatchSize;

    private Float avgTraversalSpeed;

    private Float avgExecutionTime;

    private Float avgRedToGreenTime;

}