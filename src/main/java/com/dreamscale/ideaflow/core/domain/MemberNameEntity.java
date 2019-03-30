package com.dreamscale.ideaflow.core.domain;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "member_name_view")
@Data
@EqualsAndHashCode(of = "spiritId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberNameEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID spiritId;

    private String fullName;


}
