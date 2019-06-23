package com.dreamscale.htmflow.core.domain.time;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "grid_time_twenties")
@Data
@EqualsAndHashCode(of = "tileSeq")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTimeTwentiesEntity {

    @Id
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

