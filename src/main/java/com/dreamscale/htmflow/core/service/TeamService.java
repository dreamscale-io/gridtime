package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.organization.OrganizationDto;
import com.dreamscale.htmflow.api.organization.TeamMemberWorkStatusDto;
import com.dreamscale.htmflow.api.organization.TeamWithMembersDto;
import com.dreamscale.htmflow.api.status.XPSummaryDto;
import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.api.team.TeamMemberDto;
import com.dreamscale.htmflow.core.domain.*;
import com.dreamscale.htmflow.core.exception.ValidationErrorCodes;
import com.dreamscale.htmflow.core.mapper.DtoEntityMapper;
import com.dreamscale.htmflow.core.mapper.MapperFactory;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TeamService {

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private XPService xpService;

    @Autowired
    private MasterAccountRepository masterAccountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private TeamMemberWorkStatusRepository teamMemberWorkStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TeamDto, TeamEntity> teamOutputMapper;
    private DtoEntityMapper<TeamMemberWorkStatusDto, TeamMemberWorkStatusEntity> teamMemberStatusMapper;


    @PostConstruct
    private void init() {
        teamOutputMapper = mapperFactory.createDtoEntityMapper(TeamDto.class, TeamEntity.class);
        teamMemberStatusMapper = mapperFactory.createDtoEntityMapper(TeamMemberWorkStatusDto.class, TeamMemberWorkStatusEntity.class);
    }


    public TeamDto createTeam(UUID organizationId, String teamName) {
        OrganizationEntity orgEntity = validateOrganization(organizationId);

        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setOrganizationId(orgEntity.getId());
        teamEntity.setName(teamName);
        teamRepository.save(teamEntity);

        return teamOutputMapper.toApi(teamEntity);
    }

    public List<TeamMemberDto> addMembersToTeam(UUID orgId, UUID teamId, List<UUID> membersToAdd) {
        OrganizationEntity org = validateOrganization(orgId);
        TeamEntity team = validateTeam(orgId, teamId);

        validateNotEmpty(membersToAdd);

        List<TeamMemberDto> teamMembers = new ArrayList<>();

        for (UUID memberId : membersToAdd) {
            MemberStatusEntity memberStatus = memberStatusRepository.findOne(memberId);
            validateMember(memberId, memberStatus, orgId);

            TeamMemberEntity teamMemberEntity = new TeamMemberEntity();
            teamMemberEntity.setId(UUID.randomUUID());
            teamMemberEntity.setOrganizationId(orgId);
            teamMemberEntity.setTeamId(teamId);
            teamMemberEntity.setMemberId(memberId);

            teamMemberRepository.save(teamMemberEntity);

            TeamMemberDto teamMemberDto = new TeamMemberDto();
            teamMemberDto.setOrganizationId(orgId);
            teamMemberDto.setMemberId(memberId);
            teamMemberDto.setTeamId(teamId);

            teamMemberDto.setMemberEmail(memberStatus.getEmail());
            teamMemberDto.setMemberName(memberStatus.getFullName());
            teamMemberDto.setTeamName(team.getName());

            teamMembers.add(teamMemberDto);
        }

        return teamMembers;
    }

    private void validateMember(UUID memberId, MemberStatusEntity memberStatus, UUID orgId) {
        if (memberStatus == null || !memberStatus.getOrganizationId().equals(orgId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT,
                    "Member not found in organization: "+memberId);
        }
    }

    private void validateNotEmpty(List<UUID> membersToAdd) {
        if (membersToAdd == null || membersToAdd.isEmpty()) {
            throw new BadRequestException(ValidationErrorCodes.NO_INPUTS_PROVIDED, "No members to add");
        }
    }

    private TeamEntity validateTeam(UUID orgId, UUID teamId) {
        TeamEntity teamEntity = teamRepository.findById(teamId);

        if (teamEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Team not found");
        }

        if (!orgId.equals(teamEntity.getOrganizationId())) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        return teamEntity;
    }

    private OrganizationEntity validateOrganization(UUID orgId) {
        OrganizationEntity orgEntity = organizationRepository.findById(orgId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        return orgEntity;
    }

    public List<TeamDto> getTeams(UUID orgId) {
        List<TeamEntity> teamEntityList = teamRepository.findByOrganizationId(orgId);
        return teamOutputMapper.toApiList(teamEntityList);
    }

    public List<TeamDto> getMyTeams(UUID orgId, UUID masterAccountId) {
        List<TeamEntity> teamEntityList = teamRepository.findMyTeamsByMembership(orgId, masterAccountId);
        return teamOutputMapper.toApiList(teamEntityList);
    }

    public List<TeamMemberWorkStatusDto> getStatusOfTeamMembers(UUID orgId, UUID teamId) {
        List<TeamMemberWorkStatusEntity> teamMemberStatusList =
                teamMemberWorkStatusRepository.findByOrganizationIdAndTeamId(orgId, teamId);

        List<TeamMemberWorkStatusDto> teamMemberStatusDtos = new ArrayList<>();

        for (TeamMemberWorkStatusEntity memberStatusEntity : teamMemberStatusList) {
            TeamMemberWorkStatusDto memberStatusDto = teamMemberStatusMapper.toApi(memberStatusEntity);

            XPSummaryDto xpSummary = xpService.translateToXPSummary(memberStatusEntity.getTotalXp());
            memberStatusDto.setXpSummary(xpSummary);
            teamMemberStatusDtos.add(memberStatusDto);
        }

        return teamMemberStatusDtos;
    }

    public TeamWithMembersDto getMeAndMyTeam(UUID masterAccountId) {
        OrganizationDto orgDto = organizationService.getDefaultOrganization(masterAccountId);
        MasterAccountEntity masterAccount = masterAccountRepository.findById(masterAccountId);

        List<TeamDto> teams = getTeams(orgDto.getId());

        TeamWithMembersDto teamWithMembers = null;

        if (teams.size() > 0) {
            TeamDto team = teams.get(0);

            teamWithMembers = new TeamWithMembersDto();
            teamWithMembers.setOrganizationId(team.getOrganizationId());
            teamWithMembers.setTeamId(team.getId());
            teamWithMembers.setTeamName(team.getName());

            List<TeamMemberWorkStatusDto> teamMembers = getStatusOfTeamMembers(team.getOrganizationId(), team.getId());

            for (TeamMemberWorkStatusDto teamMember : teamMembers) {
                if (teamMember.getEmail() != null && teamMember.getEmail().equalsIgnoreCase(masterAccount.getMasterEmail())) {
                    teamWithMembers.setMe(teamMember);
                } else {
                    teamWithMembers.addMember(teamMember);
                }
            }

        }

        return teamWithMembers;

    }
}
