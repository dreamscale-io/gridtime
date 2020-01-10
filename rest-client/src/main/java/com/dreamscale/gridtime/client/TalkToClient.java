package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.circuit.ChatMessageInputDto;
import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.circuit.ScreenshotReferenceInputDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.event.NewSnippetEvent;
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
            ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.CHAT_PATH)
    TalkMessageDto publishChatToRoom(@Param("talkRoomId") String talkRoomId, ChatMessageInputDto chatMessageInputDto);

    @RequestLine("GET " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{talkRoomId}")
    List<TalkMessageDto> getAllTalkMessagesFromRoom(@Param("talkRoomId") String talkRoomId);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.SNIPPET_PATH)
    TalkMessageDto publishSnippetToRoom(@Param("talkRoomId") String talkRoomId, NewSnippetEvent newSnippetEvent);


    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH +
            ResourcePaths.ROOM_PATH + "/{talkRoomId}" + ResourcePaths.SCREENSHOT_PATH)
    TalkMessageDto publishScreenshotToRoom(@Param("talkRoomId") String talkRoomId,
                                           ScreenshotReferenceInputDto screenshotReferenceInput);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.CHAT_PATH)
    TalkMessageDto publishChatToActiveRoom(ChatMessageInputDto chatMessageInputDto);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.SNIPPET_PATH)
    TalkMessageDto publishSnippetToActiveRoom(NewSnippetEvent newSnippetEvent);

    @RequestLine("POST " + ResourcePaths.TALK_PATH + ResourcePaths.TO_PATH + ResourcePaths.ROOM_PATH +
            ResourcePaths.ACTIVE_PATH + ResourcePaths.SCREENSHOT_PATH)
    TalkMessageDto publishScreenshotToActiveRoom(ScreenshotReferenceInputDto screenshotReferenceInput);

}
