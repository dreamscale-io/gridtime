package com.dreamscale.gridtime.core.domain.time;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "grid_time_calendar")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTimeCalendarEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Long tileSeq;

    private LocalDateTime clockTime;

    private String gridTime;

    private Integer year;

    private Integer block;

    private Integer blockWeek;

    private Integer day;

    private Integer dayPart;

    private Integer twentyOfTwelve;
}

