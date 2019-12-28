package com.dreamscale.gridtime.core.domain.member;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "member_name_view")
@Data
@EqualsAndHashCode(of = "memberId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberNameEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    private String shortName;
    private String fullName;

}
