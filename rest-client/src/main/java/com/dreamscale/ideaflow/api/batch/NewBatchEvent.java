package com.dreamscale.ideaflow.api.batch;

import com.dreamscale.ideaflow.api.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewBatchEvent {

    private String comment;
    private EventType type;

    private LocalDateTime position;

}
