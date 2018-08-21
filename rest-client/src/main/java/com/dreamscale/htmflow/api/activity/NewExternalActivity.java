package com.dreamscale.htmflow.api.activity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewExternalActivity {

    private Long durationInSeconds;
    private LocalDateTime endTime;

    private String comment;

}
