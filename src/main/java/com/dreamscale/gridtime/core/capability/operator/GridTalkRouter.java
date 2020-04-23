package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageMetaProp;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsRepository;
import com.dreamscale.gridtime.core.hooks.talk.TalkConnection;
import com.dreamscale.gridtime.core.hooks.talk.TalkConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GridTalkRouter {

    @Autowired
    private MemberDetailsRepository memberDetailsRepository;
    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private TalkConnectionFactory talkConnectionFactory;

    @Async
    public void sendRoomMessage(UUID roomId, TalkMessageDto talkMessageDto) {

        TalkConnection talkConnection = talkConnectionFactory.connect();

        log.debug("sendRoomMessage {} from {}", roomId, talkMessageDto.getMetaProp(TalkMessageMetaProp.FROM_USERNAME));

        talkConnection.sendRoomMessage(roomId, talkMessageDto);
    }

    @Async
    public void joinRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberDetailsEntity member = memberDetailsRepository.findByMemberId(memberId);

        log.debug("joinRoom {} for {}", roomId, member.getUsername());

        TalkConnection talkConnection = talkConnectionFactory.connect();

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {

            talkConnection.joinRoom(connectionEntity.getConnectionId(), roomId);
        }

    }

    @Async
    public void joinAllRooms(MemberConnectionEntity connection, List<TalkRoomEntity> roomsToJoin) {
        TalkConnection talkConnection = talkConnectionFactory.connect();


        log.debug("joinAllRooms for {} , found {} rooms to join.", connection.getUsername(), roomsToJoin.size());

        if (connection != null) {

            for( TalkRoomEntity room : roomsToJoin) {

                log.debug("joinRoom {} for ", room.getRoomName(), connection.getUsername());
                talkConnection.joinRoom(connection.getConnectionId(), room.getId());
            }
        }
    }

    @Async
    public void leaveAllRooms(MemberConnectionEntity connection, List<TalkRoomEntity> roomsToLeave) {
        TalkConnection talkConnection = talkConnectionFactory.connect();

        log.debug("leaveAllRooms for {} , found {} rooms to leave.", connection.getUsername(), roomsToLeave.size());

        if (connection != null) {

            for( TalkRoomEntity room : roomsToLeave) {

                log.debug("leaveRoom {} for ", room.getRoomName(), connection.getUsername());
                talkConnection.leaveRoom(connection.getConnectionId(), room.getId());
            }
        }
    }

    @Async
    public void leaveRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {
            log.debug("leaveRoom {} for {}", roomId, connectionEntity.getUsername());
            talkConnection.leaveRoom(connectionEntity.getConnectionId(), roomId);
        }
    }

    @Async
    public void closeRoom(UUID organizationId, UUID roomId) {

        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);

        TalkConnection talkConnection = talkConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {

            log.debug("closeRoom {} for {}", roomId, memberConnection.getUsername());
            talkConnection.leaveRoom(memberConnection.getConnectionId(), roomId);
        }
    }


    public void reviveRoom(UUID organizationId, UUID roomId) {

        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);

        log.debug("reviveRoom {} , found {} member connections.", roomId, connectionsInRoom.size());

        TalkConnection talkConnection = talkConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {
            log.debug("reviveRoom {} for {}", roomId, memberConnection.getUsername());
            talkConnection.joinRoom(memberConnection.getConnectionId(), roomId);
        }
    }



}
