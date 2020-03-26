package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.flow.event.NewSnippetEventDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TalkToClient {


    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.CHAT_PATH)
    TalkMessageDto publishChatToRoom(@Param("roomName") String roomName, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("GET " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}")
    List<TalkMessageDto> getAllTalkMessagesFromRoom(@Param("roomName") String talkRoomId);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SNIPPET_PATH)
    TalkMessageDto publishSnippetToRoom(@Param("roomName") String roomName, NewSnippetEventDto newSnippetEventDto);


    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.SCREENSHOT_PATH)
    TalkMessageDto publishScreenshotToRoom(@Param("roomName") String roomName,
                                           ScreenshotReferenceInputDto screenshotReferenceInput);


    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.JOIN_PATH)
    TalkMessageDto joinExistingRoom(@Param("roomName") String roomName);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{roomName}" + ResourcePaths.LEAVE_PATH)
    TalkMessageDto leaveExistingRoom(@Param("roomName") String roomName);



    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.CHAT_PATH)
    TalkMessageDto publishChatToActiveRoom(ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.SNIPPET_PATH)
    TalkMessageDto publishSnippetToActiveRoom(NewSnippetEventDto newSnippetEventDto);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.SCREENSHOT_PATH)
    TalkMessageDto publishScreenshotToActiveRoom(ScreenshotReferenceInputDto screenshotReferenceInput);

}
