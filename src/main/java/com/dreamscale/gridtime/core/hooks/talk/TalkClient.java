package com.dreamscale.gridtime.core.hooks.talk;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.core.hooks.talk.dto.ClientConnectionDto;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface TalkClient {

    @RequestLine("POST "+ TalkPaths.TALK_PATH + TalkPaths.TO_PATH + TalkPaths.CLIENT_PATH + "/{id}")
    SimpleStatusDto sendDirectMessage(@Param("id") String clientId, TalkMessageDto talkMessage);

    @RequestLine("POST "+  TalkPaths.TALK_PATH + TalkPaths.TO_PATH + TalkPaths.ROOM_PATH + "/{id}" )
    SimpleStatusDto sendRoomMessage(@Param("id") String roomId, TalkMessageDto talkMessage);

    // talk about these next vv

    @RequestLine("POST "+  TalkPaths.TALK_PATH + TalkPaths.JOIN_PATH + TalkPaths.ROOM_PATH + "/{id}")
    SimpleStatusDto joinRoom(@Param("id") String roomId, ClientConnectionDto clientConnectionDto);

    @RequestLine("POST "+  TalkPaths.TALK_PATH + TalkPaths.LEAVE_PATH + TalkPaths.ROOM_PATH + "/{id}")
    SimpleStatusDto leaveRoom(@Param("id") String roomId, ClientConnectionDto clientConnectionDto);



//    @RequestLine("POST "+  TalkPaths.TALK_PATH + TalkPaths.TO_PATH + TalkPaths.ROOM_PATH + "/{id}" + TalkPaths.RESYNC_PATH)
//    void resyncRoom(@Param("id") String roomId, ClientConnectionListInputDto clientConnections);
}
