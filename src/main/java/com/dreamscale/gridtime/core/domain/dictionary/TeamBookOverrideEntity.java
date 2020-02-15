package com.dreamscale.gridtime.core.domain.dictionary;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_book_override")
@Data
@EqualsAndHashCode(of = "teamBookTagId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamBookOverrideEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamBookTagId;

    private String tagName;

    private String definition;

    private LocalDateTime overrideDate;

    private LocalDateTime lastModifiedDate;

}
