package com.dreamscale.gridtime.core.domain.dictionary;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_dictionary_tombstone")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDictionaryTombstoneEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private String deadTagName;

    private String deadDescription;

    private LocalDateTime ripDate;

    private UUID forwardTo;

}


