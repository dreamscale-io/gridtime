package com.dreamscale.gridtime.api.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagDefinitionWithDetailsDto {

    private UUID id;
    private String tagName;
    private String definition;

    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;

    List<TagTombstoneDto> tombstones;

}
