package com.dreamscale.gridtime.core.capability.circuit;

import com.dreamscale.gridtime.api.circuit.ChatMessageDetailsDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageDto;
import com.dreamscale.gridtime.api.circuit.TalkMessageMetaProp;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionEntity;
import com.dreamscale.gridtime.core.domain.circuit.MemberConnectionRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
import com.dreamscale.gridtime.core.security.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class DirectMessageOperator {

    public static final String DIRECT_MESSAGE_URN_PREFIX = "/talk/to/client/";

    @Autowired
    ActiveWorkStatusManager activeWorkStatusManager;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private GridTalkRouter talkRouter;

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    private MemberConnectionRepository memberConnectionRepository;


    public TalkMessageDto sendDirectChatMessage(UUID organizationId, UUID fromMemberId, UUID toMemberId, String chatMessage) {

        LocalDateTime now = gridClock.now();
        Long nanoTime = gridClock.nanoTime();
        UUID messageId = UUID.randomUUID();

        MemberConnectionEntity connectionEntity = memberConnectionRepository.findByOrganizationIdAndMemberId(organizationId, toMemberId);
        validateConnection(toMemberId, connectionEntity);

        String urn = DIRECT_MESSAGE_URN_PREFIX + connectionEntity.getConnectionId();

        MemberDetailsEntity fromMemberDetails = memberDetailsRetriever.lookupMemberDetails(fromMemberId);
        MemberDetailsEntity toMemberDetails = memberDetailsRetriever.lookupMemberDetails(toMemberId);

        TalkMessageDto messageDto = new TalkMessageDto();
        messageDto.setId(messageId);
        messageDto.setUrn(urn);
        messageDto.setUri(connectionEntity.getConnectionId().toString());
        messageDto.setRequest(getRequestUriFromContext());
        messageDto.addMetaProp(TalkMessageMetaProp.FROM_MEMBER_ID, fromMemberId.toString());

        if (fromMemberDetails != null) {
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_USERNAME, fromMemberDetails.getUsername());
            messageDto.addMetaProp(TalkMessageMetaProp.FROM_FULLNAME, fromMemberDetails.getFullName());
        }

        messageDto.setMessageTime(now);
        messageDto.setNanoTime(nanoTime);
        messageDto.setMessageType(CircuitMessageType.CHAT.getSimpleClassName());
        messageDto.setData(new ChatMessageDetailsDto(chatMessage));

        talkRouter.sendDirectMessage(organizationId, toMemberDetails.getUsername(), messageDto);

        return messageDto;
    }

    private void validateConnection(UUID toMemberId, MemberConnectionEntity connectionEntity) {

        if (connectionEntity == null || connectionEntity.getConnectionId() == null) {
            throw new BadRequestException(ValidationErrorCodes.INVALID_CONNECTION, "Unable to find valid connection for member: " + toMemberId);
        }
    }


    private String getRequestUriFromContext() {

        RequestContext context = RequestContext.get();

        if (context != null) {
            return context.getRequestUri();
        } else {
            return null;
        }
    }



}
