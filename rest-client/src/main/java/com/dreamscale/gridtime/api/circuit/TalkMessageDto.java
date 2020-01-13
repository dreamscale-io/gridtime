package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkMessageDto {

    private UUID id;
    private String uri;
    private LocalDateTime messageTime;
    private Long nanoTime;
    private Map<String, String> metaProps = new LinkedHashMap<>();

    private String messageType;
    private String jsonBody;

    public void addMetaProp(String propName, String value) {
        metaProps.put(propName, value);
    }
}
