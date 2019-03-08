package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.circle.CircleDto;
import com.dreamscale.htmflow.api.organization.*;
import com.dreamscale.htmflow.api.spirit.XPSummaryDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MemberStatusService {

    @Autowired
    private SpiritService xpService;

    @Autowired
    private CircleService circleService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<MemberWorkStatusDto, MemberStatusEntity> memberStatusMapper;

    @Autowired
    private TimeService timeService;

    @PostConstruct
    private void init() {
        memberStatusMapper = mapperFactory.createDtoEntityMapper(MemberWorkStatusDto.class, MemberStatusEntity.class);
    }

    public MemberWorkStatusDto getMyCurrentStatus(UUID organizationId, UUID memberId) {

        MemberStatusEntity memberStatusEntity = memberStatusRepository.findById(memberId);
        log.debug("Who am I? "+memberStatusEntity.getId() + ", "+memberStatusEntity.getFullName());

        return toDto(memberStatusEntity);
    }

    public List<MemberWorkStatusDto> getStatusOfMeAndMyTeam(UUID organizationId, UUID memberId) {

        List<TeamEntity> teamEntityList = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        UUID teamId = teamEntityList.get(0).getId();

        LinkedList<MemberWorkStatusDto> memberWorkStatusDtos = new LinkedList<>();

        List<MemberStatusEntity> teamMemberStatusEntities = memberStatusRepository.findByTeamIdAndNotMe(teamId, memberId);
        for (MemberStatusEntity memberStatusEntity : teamMemberStatusEntities) {
            memberWorkStatusDtos.add(toDto(memberStatusEntity));
        }
        sortMembers(memberWorkStatusDtos);

        memberWorkStatusDtos.addFirst(getMyCurrentStatus(organizationId, memberId));

        return memberWorkStatusDtos;
    }

    private MemberWorkStatusDto toDto(MemberStatusEntity memberStatusEntity) {
        MemberWorkStatusDto memberStatusDto = memberStatusMapper.toApi(memberStatusEntity);

        XPSummaryDto xpSummary = xpService.translateToXPSummary(memberStatusEntity.getTotalXp());
        memberStatusDto.setXpSummary(xpSummary);
        memberStatusDto.setShortName(createShortName(memberStatusEntity.getFullName()));

        if (memberStatusEntity.getActiveCircleId() != null) {
            CircleDto circleDto = circleService.getCircle(memberStatusEntity.getId(), memberStatusEntity.getActiveCircleId());

            memberStatusDto.setActiveCircle(circleDto);
        }

        return memberStatusDto;
    }



    private String createShortName(String fullName) {
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
            if (compare == 0 && member1.getShortName() != null && member2.getShortName() != null) {
                compare = member1.getShortName().compareTo(member2.getShortName());
            }
            return compare;
        });

    }


}
