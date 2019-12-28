package com.dreamscale.gridtime.api.router;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkConnectionStatusDto {

    private UUID connectionId;
    private TalkStatusCode statusCode;
    private String message;

}
