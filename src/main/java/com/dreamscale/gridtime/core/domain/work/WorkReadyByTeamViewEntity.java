package com.dreamscale.gridtime.core.domain.work;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "work_ready_by_team_view")
@Data
@Builder
@EqualsAndHashCode(of = "workId")
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class WorkReadyByTeamViewEntity {

    @EmbeddedId
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private WorkId workId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID calendarId;

    @Enumerated(EnumType.STRING)
    private WorkToDoType workToDoType;

    private LocalDateTime earliestEventTime;

    private Integer tileCount;

    private Integer teamSize;

}
