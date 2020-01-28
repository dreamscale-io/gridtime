package com.dreamscale.gridtime.api.flow.batch;

import com.dreamscale.gridtime.api.flow.event.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewFlowBatchEventDto {

    private String comment;
    private EventType type;

    private LocalDateTime position;

}
