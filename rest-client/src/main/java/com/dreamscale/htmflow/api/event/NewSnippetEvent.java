package com.dreamscale.htmflow.api.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewSnippetEvent {

    private String comment;

    private EventType eventType;
    private LocalDateTime position;

    private String source;
    private String snippet;
}
