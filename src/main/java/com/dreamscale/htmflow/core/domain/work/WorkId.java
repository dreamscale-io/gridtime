package com.dreamscale.htmflow.core.domain.work;

import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class WorkId implements Serializable {

    @Column(name = "team_id")
    private UUID teamId;

    @Column(name = "grid_time")
    private String gridTime;

}
