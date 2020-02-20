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
public class WordTombstoneDto {

    private String deadWordName;
    private String deadDefinition;

    private LocalDateTime ripDate;
    private LocalDateTime reviveDate;

    private UUID ripByMemberId;

}

