package com.dreamscale.gridtime.core.domain.terminal;

import com.dreamscale.gridtime.api.terminal.Command;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "terminal_circuit_command_history")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TerminalCircuitCommandHistoryEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID circuitId;

    @Enumerated(EnumType.STRING)
    private Command command;

    private String args;

    private LocalDateTime commandDate;

}
