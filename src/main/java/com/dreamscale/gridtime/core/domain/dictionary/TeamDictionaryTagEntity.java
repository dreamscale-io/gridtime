package com.dreamscale.gridtime.core.domain.dictionary;

import com.dreamscale.gridtime.api.dictionary.TagTombstoneDto;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity(name = "team_dictionary_tag")
@Data
@EqualsAndHashCode(of = "id")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamDictionaryTagEntity {

    @Id
    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID id;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID organizationId;

    @org.hibernate.annotations.Type(type = "org.hibernate.type.PostgresUUIDType")
    private UUID teamId;

    private String tagName;

    private String definition;

    private LocalDateTime creationDate;

    private LocalDateTime lastModifiedDate;

}

