package com.dreamscale.htmflow.core.domain.tile;

import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "story_tile")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryTileEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID torchieId;

    private String uri;
    private String zoomLevel;
    private LocalDateTime clockPosition;

    private String dreamTime;

    private int year;
    private int block;
    private int weeksIntoBlock;
    private int weeksIntoYear;
    private int daysIntoWeek;
    private int fourHourSteps;
    private int twentyMinuteSteps;

    private String jsonTile;
    private String jsonTileSummary;
}
