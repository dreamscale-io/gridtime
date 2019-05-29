package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.type.FeatureType;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "grid_feature")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridFeatureEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    @Enumerated(EnumType.STRING)
    private FeatureType featureType;

    private String searchKey;

    private String json;

}

