package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.journal.JournalEntryDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.capability.active.MemberCapability;
import com.dreamscale.gridtime.core.capability.directory.DictionaryCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamCapability;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.domain.member.MemberStatusRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.mapping.SillyNameGenerator;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class RoomOperator {

    public static final String ROOM_URN_PREFIX = "/talk/to/room/";


    @Autowired
    private TalkRoomRepository talkRoomRepository;

    @Autowired
    private TalkRoomMemberRepository talkRoomMemberRepository;

    @Autowired
    ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private MemberDetailsService memberDetailsService;



    @Transactional
    public void joinRoom(UUID organizationId, UUID memberId, UUID roomId) {

        TalkRoomEntity roomEntity = talkRoomRepository.findByOrganizationIdAndId(organizationId, roomId);

        validateRoomIsFound(roomEntity, "roomId :" + roomId.toString());

        joinTheGridRoomAndTalkRoom(organizationId, memberId, roomEntity);
    }


    @Transactional
    public void joinRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = talkRoomRepository.findByOrganizationIdAndRoomName(organizationId, roomName);

        validateRoomIsFound(roomEntity, roomName);

        joinTheGridRoomAndTalkRoom(organizationId, memberId, roomEntity);

    }

    private void joinTheGridRoomAndTalkRoom(UUID organizationId, UUID memberId, TalkRoomEntity roomEntity) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        String roomName = roomEntity.getRoomName();

        log.debug("[RoomOperator] Member {} joining room {} at {}", memberId, roomName, nanoTime);

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity == null) {
            roomMemberEntity = new TalkRoomMemberEntity();
            roomMemberEntity.setId(UUID.randomUUID());
            roomMemberEntity.setRoomId(roomEntity.getId());
            roomMemberEntity.setOrganizationId(organizationId);
            roomMemberEntity.setMemberId(memberId);
            roomMemberEntity.setJoinTime(now);

            talkRoomMemberRepository.save(roomMemberEntity);

        } else {
            log.warn("[RoomOperator] Member {} already joined {}", memberId, roomName);
        }

        talkRouter.joinRoom(organizationId, memberId, roomEntity.getId());
    }

    @Transactional
    public void leaveRoom(UUID organizationId, UUID memberId, String roomName) {

        TalkRoomEntity roomEntity = talkRoomRepository.findByOrganizationIdAndRoomName(organizationId, roomName);
        validateRoomIsFound(roomEntity, roomName);

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();

        log.debug("[RoomOperator] Member {} leaving room {}", memberId, roomName);

        TalkRoomMemberEntity roomMemberEntity = talkRoomMemberRepository.findByOrganizationIdAndRoomIdAndMemberId(organizationId, roomEntity.getId(), memberId);

        if (roomMemberEntity != null) {

            talkRouter.leaveRoom(organizationId, memberId, roomEntity.getId());

            talkRoomMemberRepository.delete(roomMemberEntity);
        }
    }

    private void validateRoomIsFound(TalkRoomEntity roomEntity, String roomName) {
        if (roomEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ROOM, "Unable to find: " + roomName);
        }
    }

}
