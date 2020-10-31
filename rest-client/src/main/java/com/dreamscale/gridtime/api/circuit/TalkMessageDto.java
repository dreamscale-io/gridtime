package com.dreamscale.gridtime.api.circuit;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TalkMessageDto {

    private UUID id;
    private String urn;   //this should be the friendly /circuit/wtf/{room}
    private String uri;   //this should be the global unique id of the room
    private String request;  //this should be the grid request context uri
    private LocalDateTime messageTime;
    private Long nanoTime;
    private Map<String, String> metaProps;

    private String messageType;

    @JsonTypeInfo(use= JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.PROPERTY, property="clazz")
    private Object data;

    public void addMetaProp(TalkMessageMetaProp prop, String value) {
        if (metaProps == null) {
            metaProps = new LinkedHashMap<>();
        }
        metaProps.put(prop.getPropName(), value);
    }

    public String getMetaProp(TalkMessageMetaProp metaPropType) {
        if (metaProps != null) {
            return metaProps.get(metaPropType.getPropName());
        }

        return null;
    }
}
