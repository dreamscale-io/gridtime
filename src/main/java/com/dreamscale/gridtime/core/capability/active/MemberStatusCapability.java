package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import com.dreamscale.gridtime.core.capability.operator.LearningCircuitOperator;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.operator.SpiritNetworkOperator;
import com.dreamscale.gridtime.core.service.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MemberStatusCapability {

    @Autowired
    private SpiritNetworkOperator xpService;

    @Autowired
    private LearningCircuitOperator learningCircuitOperator;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<MemberWorkStatusDto, MemberStatusEntity> memberStatusMapper;

    @Autowired
    private GridClock gridClock;

    @PostConstruct
    private void init() {
        memberStatusMapper = mapperFactory.createDtoEntityMapper(MemberWorkStatusDto.class, MemberStatusEntity.class);
    }

    public MemberWorkStatusDto getMyCurrentStatus(UUID organizationId, UUID memberId) {

        MemberStatusEntity memberStatusEntity = memberStatusRepository.findById(memberId);
        log.debug("Who am I? "+memberStatusEntity.getId() + ", "+memberStatusEntity.getFullName());

        return toDtoWithDetails(memberStatusEntity);
    }


    public List<MemberStatusEntity> getTeamMemberStatuses(UUID teamId) {

        return memberStatusRepository.findByTeamId(teamId);

    }

    public List<MemberWorkStatusDto> getStatusOfMeAndMyTeam(UUID organizationId, UUID memberId) {

        List<TeamEntity> teamEntityList = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        UUID teamId = teamEntityList.get(0).getId();

        LinkedList<MemberWorkStatusDto> memberWorkStatusDtos = new LinkedList<>();

        List<MemberStatusEntity> teamMemberStatusEntities = memberStatusRepository.findByTeamIdAndNotMe(teamId, memberId);
        for (MemberStatusEntity memberStatusEntity : teamMemberStatusEntities) {
            memberWorkStatusDtos.add(toDtoWithDetails(memberStatusEntity));
        }
        sortMembers(memberWorkStatusDtos);

        memberWorkStatusDtos.addFirst(getMyCurrentStatus(organizationId, memberId));

        return memberWorkStatusDtos;
    }

    public MemberWorkStatusDto getStatusOfMember(UUID organizationId, UUID memberId) {

        MemberStatusEntity memberStatusEntity = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        return toDtoWithDetails(memberStatusEntity);
    }


    private MemberWorkStatusDto toDtoWithDetails(MemberStatusEntity memberStatusEntity) {
        MemberWorkStatusDto memberStatusDto = memberStatusMapper.toApi(memberStatusEntity);

        XPSummaryDto xpSummary = xpService.translateToXPSummary(memberStatusEntity.getTotalXp());
        memberStatusDto.setXpSummary(xpSummary);
        memberStatusDto.setDisplayName(createDisplayName(memberStatusEntity.getFullName()));

        if (memberStatusEntity.getActiveCircuitId() != null) {
            LearningCircuitDto circuitDto = learningCircuitOperator.getCircuit(memberStatusEntity.getOrganizationId(),
                    memberStatusEntity.getActiveCircuitId());

            memberStatusDto.setActiveCircuit(circuitDto);
        }

        return memberStatusDto;
    }



    private String createDisplayName(String fullName) {
        String shortName = fullName;
        if (fullName != null && fullName.contains(" ")) {
            shortName = fullName.substring(0, fullName.indexOf(" "));
        }
        return shortName;
    }

    private void sortMembers(List<MemberWorkStatusDto> teamMembers) {

        teamMembers.sort((member1, member2) -> {
            int compare = 0;

            if (member1.getOnlineStatus() != null && member2.getOnlineStatus() != null) {
                Integer orderMember1 = member1.getOnlineStatus().getOrder();
                Integer orderMember2 = member2.getOnlineStatus().getOrder();

                compare = orderMember1.compareTo(orderMember2);
            }
            if (compare == 0 && member1.getDisplayName() != null && member2.getDisplayName() != null) {
                compare = member1.getDisplayName().compareTo(member2.getDisplayName());
            }
            return compare;
        });

    }



}
