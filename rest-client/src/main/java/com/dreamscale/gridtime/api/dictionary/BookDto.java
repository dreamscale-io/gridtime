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
public class BookDto {

    UUID id;
    UUID teamId;
    String bookName;

    LocalDateTime creationDate;
    LocalDateTime lastModifiedDate;

    List<TagDefinitionDto> definitions;

}

