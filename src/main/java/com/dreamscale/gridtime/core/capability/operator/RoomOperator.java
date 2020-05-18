package com.dreamscale.gridtime.core.capability.operator;

import com.dreamscale.gridtime.api.circuit.*;
import com.dreamscale.gridtime.api.journal.JournalEntryDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import com.dreamscale.gridtime.api.team.TeamCircuitDto;
import com.dreamscale.gridtime.api.team.TeamCircuitRoomDto;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.capability.active.MemberCapability;
import com.dreamscale.gridtime.core.capability.directory.OrganizationCapability;
import com.dreamscale.gridtime.core.capability.directory.TeamCapability;
import com.dreamscale.gridtime.core.domain.circuit.*;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageEntity;
import com.dreamscale.gridtime.core.domain.circuit.message.TalkRoomMessageRepository;
import com.dreamscale.gridtime.core.domain.member.MemberDetailsEntity;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.hooks.talk.dto.CircuitMessageType;
import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;
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



}
