package com.dreamscale.htmflow.core.domain.time;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity(name = "grid_time_weeks")
@Data
@EqualsAndHashCode(of = "tileSeq")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTimeWeeksEntity {

    @Id
    private Long tileSeq;

    private LocalDateTime clockTime;

    private String gridTime;

    private Integer year;

    private Integer block;

    private Integer blockWeek;
}

