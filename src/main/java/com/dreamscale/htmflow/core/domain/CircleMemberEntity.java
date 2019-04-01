package com.dreamscale.htmflow.core.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity(name = "circle_member")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleMemberEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;
    private UUID circleId;
    private UUID spiritId;

}
