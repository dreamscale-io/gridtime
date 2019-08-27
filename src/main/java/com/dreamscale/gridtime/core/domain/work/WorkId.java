package com.dreamscale.gridtime.core.domain.work;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
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
