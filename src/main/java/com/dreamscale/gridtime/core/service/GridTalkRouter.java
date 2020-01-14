package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.core.domain.active.ActiveAccountStatusRepository;
import com.dreamscale.gridtime.core.hooks.talk.TalkConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class GridTalkRouter {


    @Autowired
    private ActiveAccountStatusRepository activeAccountStatusRepository;

    @Autowired
    private TalkConnectionFactory talkConnectionFactory;

    public void sendAsyncRoomMessage(TalkMessageDto talkMessageDto) {

    }

    public void joinRoom(UUID organizationId, UUID memberId, String talkRoomId) {

    }

    public void leaveRoom(UUID organizationId, UUID memberId, String talkRoomId) {

    }

    public void closeRoom(UUID organizationId, String deriveWTFTalkRoomId) {


    }

    public void reviveRoom(UUID organizationId, String deriveWTFTalkRoomId) {

    }
}
