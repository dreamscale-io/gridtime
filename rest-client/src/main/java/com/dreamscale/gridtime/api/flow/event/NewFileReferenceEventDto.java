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
public class NewFileReferenceEventDto {

    private String comment;

    private LocalDateTime position;

    String fileName;
    String fileType;
    String fileUri;
}
