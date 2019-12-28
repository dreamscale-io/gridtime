package com.dreamscale.gridtime.core.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class TalkRouterService {

    public void sendAsyncRoomMessage(UUID messageId, LocalDateTime now, UUID fromMemberId, String talkRoomId, String statusMessage) {

    }
}
