package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "grid_tile_idea_flow")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTileIdeaFlowEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSeq;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID lastIdeaFlowState;

    private Long timeInTile;

    private Double avgFlame;

    private Double percentWtf;

    private Double percentLearning;

    private Double percentProgress;

    private Double percentPairing;

}
