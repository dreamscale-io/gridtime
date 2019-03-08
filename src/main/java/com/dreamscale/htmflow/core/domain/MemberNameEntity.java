package com.dreamscale.htmflow.core.domain;

import com.dreamscale.htmflow.api.circle.MessageType;
import com.dreamscale.htmflow.core.domain.flow.MetadataFields;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
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
