package com.dreamscale.gridtime.core.capability.active;

import com.dreamscale.gridtime.api.circuit.LearningCircuitDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.spirit.XPSummaryDto;
import com.dreamscale.gridtime.core.capability.circuit.WTFCircuitOperator;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.circuit.TorchieNetworkOperator;
import com.dreamscale.gridtime.core.capability.system.GridClock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MemberStatusManager {

    @Autowired
    private TorchieNetworkOperator xpService;

    @Autowired
    private WTFCircuitOperator wtfCircuitOperator;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TeamMemberDto, MemberStatusEntity> memberStatusMapper;

    @Autowired
    private GridClock gridClock;

    @Autowired
    private EntityManager entityManager;

    @PostConstruct
    private void init() {
        memberStatusMapper = mapperFactory.createDtoEntityMapper(TeamMemberDto.class, MemberStatusEntity.class);
    }

    public TeamMemberDto getMyCurrentStatus(UUID organizationId, UUID memberId) {
        MemberStatusEntity memberStatusEntity = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);
        return toDtoWithDetails(memberStatusEntity);
    }

    public List<MemberStatusEntity> getTeamMemberStatuses(UUID teamId) {

        return memberStatusRepository.findByTeamId(teamId);
    }

    public List<TeamMemberDto> getMembersForMeAndMyTeam(UUID organizationId, UUID memberId, UUID teamId) {

        List<MemberStatusEntity> teamMemberStatusEntities = memberStatusRepository.findByTeamIdAndNotMe(teamId, memberId);

        MemberStatusEntity meStatus = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        return toStatusDtos(meStatus, teamMemberStatusEntities);
    }

    public List<TeamMemberDto> getMembersForTeamWithMeFirst(UUID organizationId, UUID teamId, UUID memberId) {

        List<MemberStatusEntity> teamMemberStatusEntities = memberStatusRepository.findByTeamId(teamId);

        MemberStatusEntity meStatus = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        return toStatusDtos(meStatus, teamMemberStatusEntities);
    }

    public List<TeamMemberDto> getMembersForMeAndMyTeam(UUID organizationId, UUID memberId) {

        List<TeamEntity> teamEntityList = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        UUID teamId = teamEntityList.get(0).getId();

        LinkedList<TeamMemberDto> teamMemberDtos = new LinkedList<>();

        List<MemberStatusEntity> teamMemberStatusEntities = memberStatusRepository.findByTeamIdAndNotMe(teamId, memberId);
        MemberStatusEntity meStatus = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        toStatusDtos(meStatus, teamMemberStatusEntities);

        return teamMemberDtos;
    }

    private LinkedList<TeamMemberDto> toStatusDtos(MemberStatusEntity meStatus, List<MemberStatusEntity> teamMemberStatusEntities) {
        LinkedList<TeamMemberDto> teamMemberDtos = new LinkedList<>();

        for (MemberStatusEntity memberStatusEntity : teamMemberStatusEntities) {
            if (isNotMe(meStatus, memberStatusEntity)) {
                teamMemberDtos.add(toDtoWithDetails(memberStatusEntity));
            }
        }
        sortMembers(teamMemberDtos);

        if (meStatus != null) {
            teamMemberDtos.addFirst(toDtoWithDetails(meStatus));
        }

        return teamMemberDtos;
    }

    private boolean isNotMe(MemberStatusEntity meStatus, MemberStatusEntity memberStatusEntity) {
        return meStatus != null && !meStatus.getId().equals(memberStatusEntity.getId());
    }


    public TeamMemberDto getStatusOfMember(UUID organizationId, UUID memberId) {

        MemberStatusEntity memberStatusEntity = memberStatusRepository.findByOrganizationIdAndId(organizationId, memberId);

        return toDtoWithDetails(memberStatusEntity);
    }

    private TeamMemberDto toDtoWithDetails(MemberStatusEntity memberStatusEntity) {
        TeamMemberDto memberStatusDto = memberStatusMapper.toApi(memberStatusEntity);

        XPSummaryDto xpSummary = xpService.translateToXPSummary(memberStatusEntity.getTotalXp());
        memberStatusDto.setXpSummary(xpSummary);
        memberStatusDto.setDisplayName(createDisplayName(memberStatusEntity.getFullName()));

        if (memberStatusEntity.getActiveCircuitId() != null) {
            LearningCircuitDto circuitDto = wtfCircuitOperator.getCircuit(memberStatusEntity.getOrganizationId(),
                    memberStatusEntity.getActiveCircuitId());

            memberStatusDto.setActiveCircuit(circuitDto);

            if (memberStatusEntity.getId().equals(circuitDto.getOwnerId())) {
                memberStatusDto.setActiveJoinType(CircuitJoinType.OWNER);
            } else {
                memberStatusDto.setActiveJoinType(CircuitJoinType.TEAM_MEMBER);
            }
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

    private void sortMembers(List<TeamMemberDto> teamMembers) {

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


    public TeamMemberDto findMemberByEmail(UUID organizationId, String email) {

        MemberStatusEntity memberStatus = memberStatusRepository.findByOrganizationIdAndEmail(organizationId, email);
        return toDtoWithDetails(memberStatus);
    }

    public TeamMemberDto findMemberWithUserName(UUID organizationId, String username) {
        return null;
    }
}
