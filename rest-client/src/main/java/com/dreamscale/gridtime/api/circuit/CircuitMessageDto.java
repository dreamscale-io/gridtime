package com.dreamscale.gridtime.api.circuit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircuitMessageDto {

    private UUID messageId;
    private LocalDateTime messageTime;
    private String messageType;

    private CircuitMemberDto messageFrom;

    //optional fields
    private String message;
    private String fileName;
    private String filePath;
    private String snippetSource;
    private String snippet;

}

//    TalkRoomMessageEntity messageEntity = new TalkRoomMessageEntity();
//        messageEntity.setId(messageId);
//                messageEntity.setFromId(fromMemberId);
//                messageEntity.setToRoomId(learningCircuitEntity.getWtfRoomId());
//                messageEntity.setMessageTime(now);
//                messageEntity.setMessageType(MessageType.CHAT);
//                messageEntity.setJsonBody(JSONTransformer.toJson(new ChatMessageDto(chatMessage)));
//
