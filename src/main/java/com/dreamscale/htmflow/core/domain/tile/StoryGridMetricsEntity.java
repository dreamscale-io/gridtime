package com.dreamscale.htmflow.core.domain.tile;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "story_grid_metrics")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryGridMetricsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID torchieId;
    private UUID tileId;
    private String featureUri;

    private String candleType;

    private Integer sampleCount;
    private Double total;
    private Double avg;
    private Double stddev;
    private Double min;
    private Double max;
}

