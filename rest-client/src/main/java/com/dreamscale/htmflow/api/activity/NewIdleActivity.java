package com.dreamscale.htmflow.api.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewIdleActivity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

}
