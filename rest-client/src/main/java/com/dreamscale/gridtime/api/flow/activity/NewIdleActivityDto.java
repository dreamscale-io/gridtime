package com.dreamscale.gridtime.api.flow.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewIdleActivityDto implements Activity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

}
