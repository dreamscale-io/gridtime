package com.dreamscale.gridtime.core.domain.circuit;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "member_connection_view")
@Data
@EqualsAndHashCode(of = "memberId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberConnectionEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    private UUID connectionId;

    private LocalDateTime lastActivity;
    private LocalDateTime lastHeartbeat;

    private String shortName;
    private String fullName;

}
