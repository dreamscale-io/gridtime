package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.math.BigInteger;
import java.util.UUID;

@Entity(name = "grid_marker")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridMarkerEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    private String rowName;

    private BigInteger tileSequence;

    private Integer beatNumber;

    private String startOrStop;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID gridFeatureId;

}

