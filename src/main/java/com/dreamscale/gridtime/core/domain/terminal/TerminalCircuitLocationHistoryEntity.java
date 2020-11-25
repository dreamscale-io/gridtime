package com.dreamscale.gridtime.core.domain.terminal;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "terminal_circuit_location_history")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCircuitLocationHistoryEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    @Enumerated(EnumType.STRING)
    private ZoomLevel zoomLevel;

    private Integer tileSeq;

    private LocalDateTime movementDate;

}
