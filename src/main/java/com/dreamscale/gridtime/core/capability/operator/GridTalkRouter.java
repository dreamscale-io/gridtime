package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageMetaProp;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsRepository;
import com.dreamscale.gridtime.core.hooks.talk.TalkClientConnection;
import com.dreamscale.gridtime.core.hooks.talk.TalkClientConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class GridTalkRouter {

    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    private MemberDetailsRepository memberDetailsRepository;
    @Autowired
    private MemberConnectionRepository memberConnectionRepository;

    @Autowired
    private TalkClientConnectionFactory talkClientConnectionFactory;

    public void sendRoomMessage(UUID roomId, TalkMessageDto talkMessageDto) {

        TalkRoomEntity room = talkRoomRepository.findById(roomId);
        String userName = talkMessageDto.getMetaProp(TalkMessageMetaProp.FROM_USERNAME);

        log.debug("[GridTalkRouter] sendRoomMessage from {} to {}", userName, room.getRoomName());

        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        talkClientConnection.sendRoomMessage(roomId, talkMessageDto, userName, room.getRoomName());
    }

    public void joinRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberDetailsEntity member = memberDetailsRepository.findByMemberId(memberId);
        TalkRoomEntity room = talkRoomRepository.findById(roomId);

        log.debug("[GridTalkRouter] joinRoom {} for {}", room.getRoomName(), member.getUsername());

        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {

            talkClientConnection.joinRoom(connectionEntity.getConnectionId(), roomId, connectionEntity.getUsername(), room.getRoomName());
        }

    }

    public void joinAllRooms(MemberConnectionEntity connection, List<TalkRoomEntity> roomsToJoin) {
        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();


        log.debug("[GridTalkRouter] joinAllRooms for {} , found {} rooms to join.", connection.getUsername(), roomsToJoin.size());

        if (connection != null) {

            for( TalkRoomEntity room : roomsToJoin) {

                log.debug("[GridTalkRouter] joinRoom {} for {}", room.getRoomName(), connection.getUsername());
                talkClientConnection.joinRoom(connection.getConnectionId(), room.getId(), connection.getUsername(), room.getRoomName());
            }
        }
    }

    //TODO I'm shifting in favor of pushing this functionality into the GridTalkRouter.

    //consider this a work in progress...

    @Transactional
    public void leaveAllRooms(MemberConnectionEntity connection) {

        List<TalkRoomEntity> talkRooms = talkRoomRepository.findRoomsByMembership(connection.getOrganizationId(), connection.getMemberId());

        leaveAllRooms(connection, talkRooms);

        talkRoomMemberRepository.deleteFromAllRooms(connection.getMemberId());

    }

    public void leaveAllRooms(MemberConnectionEntity connection, List<TalkRoomEntity> roomsToLeave) {
        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        log.debug("[GridTalkRouter] leaveAllRooms for {} , found {} rooms to leave.", connection.getUsername(), roomsToLeave.size());

        if (connection != null) {

            for( TalkRoomEntity room : roomsToLeave) {

                log.debug("[GridTalkRouter] leaveRoom {} for {}", room.getRoomName(), connection.getUsername());
                talkClientConnection.leaveRoom(connection.getConnectionId(), room.getId(), connection.getUsername(), room.getRoomName());
            }
        }
    }

    public void leaveRoom(UUID organizationId, UUID memberId, UUID roomId) {

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, memberId);
        TalkRoomEntity room = talkRoomRepository.findById(roomId);

        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        if (connectionEntity != null && connectionEntity.getConnectionId() != null) {
            log.debug("[GridTalkRouter] leaveRoom {} for {}", room.getRoomName(), connectionEntity.getUsername());
            talkClientConnection.leaveRoom(connectionEntity.getConnectionId(), roomId, connectionEntity.getUsername(), room.getRoomName() );
        }
    }

    public void closeRoom(UUID organizationId, UUID roomId) {

        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);
        TalkRoomEntity room = talkRoomRepository.findById(roomId);

        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {
            log.debug("[GridTalkRouter] closeRoom {} for {}", roomId, memberConnection.getUsername());
            talkClientConnection.leaveRoom(memberConnection.getConnectionId(), roomId, memberConnection.getUsername(), room.getRoomName());
        }
    }

    public void reviveRoom(UUID organizationId, UUID roomId) {

        List<MemberConnectionEntity> connectionsInRoom = memberConnectionRepository.findByConnectionsInTalkRoom(organizationId, roomId);
        TalkRoomEntity room = talkRoomRepository.findById(roomId);

        log.debug("[GridTalkRouter] reviveRoom {} , found {} member connections.", room.getRoomName(), connectionsInRoom.size());

        TalkClientConnection talkClientConnection = talkClientConnectionFactory.connect();

        for (MemberConnectionEntity memberConnection : connectionsInRoom) {
            log.debug("[GridTalkRouter] reviveRoom {} for {}", room.getRoomName(), memberConnection.getUsername());
            talkClientConnection.joinRoom(memberConnection.getConnectionId(), roomId, memberConnection.getUsername(), room.getRoomName());
        }
    }


}
