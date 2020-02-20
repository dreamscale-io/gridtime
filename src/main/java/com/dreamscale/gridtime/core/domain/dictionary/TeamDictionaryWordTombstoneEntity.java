package com.dreamscale.gridtime.core.domain.dictionary;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_dictionary_word_tombstone")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDictionaryWordTombstoneEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    private UUID organizationId;

    private UUID teamId;

    private UUID ripByMemberId;

    private String lowerCaseWordName;

    private String deadWordName;

    private String deadDefinition;

    private LocalDateTime ripDate;

    private LocalDateTime reviveDate;

    private UUID forwardTo;

}


