package com.dreamscale.htmflow.api.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentionFinishInputDto {

    private FinishStatus finishStatus;
}
