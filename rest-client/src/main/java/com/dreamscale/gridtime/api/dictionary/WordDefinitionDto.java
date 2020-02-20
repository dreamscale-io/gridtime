package com.dreamscale.gridtime.api.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDefinitionDto {

    private String wordName;
    private String definition;

    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    private UUID createdByMemberId;
    private UUID lastModifiedByMemberId;

    private boolean isOverride;
}
