package com.dreamscale.htmflow.core.domain.member;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "member_name_view")
@Data
@EqualsAndHashCode(of = "torchieId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberNameEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID torchieId;

    private String fullName;


}
