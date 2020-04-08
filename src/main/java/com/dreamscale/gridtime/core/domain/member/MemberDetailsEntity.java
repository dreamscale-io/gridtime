package com.dreamscale.gridtime.core.domain.member;

import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity(name = "member_details_view")
@Data
@EqualsAndHashCode(of = "memberId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetailsEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID memberId;

    private String username;
    private String displayName;
    private String fullName;

}
