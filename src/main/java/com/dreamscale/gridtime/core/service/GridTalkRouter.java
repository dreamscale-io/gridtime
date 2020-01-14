package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionEntity;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionRepository;
import com.dreamscale.gridtime.core.domain.circuit.TalkRoomMemberRepository;
import com.dreamscale.gridtime.core.hooks.talk.TalkConnection;
import com.dreamscale.gridtime.core.hooks.talk.TalkConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GridTalkRouter {

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private TalkConnectionFactory talkConnectionFactory;

    public void sendAsyncRoomMessage(UUID roomId, TalkMessageDto talkMessageDto) {

        TalkConnection talkConnection = talkConnectionFactory.connect();

        talkConnection.sendRoomMessage(roomId, talkMessageDto);
    }

    public void joinRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {

            talkConnection.joinRoom(connectionEntity.getConnectionId(), roomId);
        }

    }

    public void leaveRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {
            talkConnection.leaveRoom(connectionEntity.getConnectionId(), roomId);
        }
    }

    public void closeRoom(UUID organizationId, UUID roomId) {

        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {

            talkConnection.leaveRoom(memberConnection.getConnectionId(), roomId);
        }
    }

    public void reviveRoom(UUID organizationId, UUID roomId) {
        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {
            talkConnection.joinRoom(memberConnection.getConnectionId(), roomId);
        }
    }
}
