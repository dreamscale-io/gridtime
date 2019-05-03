package com.dreamscale.htmflow.core.domain.tile;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "story_grid_summary")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryGridSummaryEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID tileId;

    private double averageMood;
    private double percentLearning;
    private double percentProgress;
    private double percentTroubleshooting;
    private double percentPairing;

    private int boxesVisited;
    private int locationsVisited;
    private int traversalsVisited;
    private int bridgesVisited;
    private int bubblesVisited;

    private int totalExperiments;
    private int totalMessages;
}
