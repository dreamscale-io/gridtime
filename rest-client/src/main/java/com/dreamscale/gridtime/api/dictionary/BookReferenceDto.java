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
public class BookReferenceDto {

    UUID id;
    UUID teamId;
    String bookName;

    UUID createdByMemberId;

    String bookStatus;

    LocalDateTime creationDate;

    LocalDateTime lastModifiedDate;

}

