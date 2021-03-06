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
public class NewExternalActivityDto implements Activity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

    private String comment;

}
