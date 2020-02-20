package com.dreamscale.gridtime.core.domain.dictionary;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "team_book_word_override")
@Data
@EqualsAndHashCode(of = "teamBookWordId")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamBookWordOverrideEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamBookWordId;

    private UUID teamBookId;

    private String wordName;

    private String definition;

    private UUID createdByMemberId;

    private UUID lastModifiedByMemberId;

    private String lowerCaseWordName;

    private LocalDateTime overrideDate;

    private LocalDateTime lastModifiedDate;

}
