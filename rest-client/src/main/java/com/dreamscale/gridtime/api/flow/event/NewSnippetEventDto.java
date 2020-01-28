package com.dreamscale.gridtime.api.flow.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewSnippetEventDto {

    private LocalDateTime position;

    private String filePath;
    private Integer lineNumber;

    private SnippetSourceType source;
    private String snippet;

}
