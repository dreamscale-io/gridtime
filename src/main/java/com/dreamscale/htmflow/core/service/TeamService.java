package com.dreamscale.htmflow.core.service;

import com.dreamscale.htmflow.api.team.TeamDto;
import com.dreamscale.htmflow.api.team.TeamMemberDto;
import com.dreamscale.htmflow.api.team.TeamMembersToAddInputDto;
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
    private OrganizationRepository organizationRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TeamDto, TeamEntity> teamOutputMapper;

    @PostConstruct
    private void init() {
        teamOutputMapper = mapperFactory.createDtoEntityMapper(TeamDto.class, TeamEntity.class);
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

    public List<TeamMemberDto> addMembersToTeam(UUID orgId, UUID teamId, TeamMembersToAddInputDto membersToAdd) {
        OrganizationEntity org = validateOrganization(orgId);
        TeamEntity team = validateTeam(orgId, teamId);

        validateNotEmpty(membersToAdd);

        List<TeamMemberDto> teamMembers = new ArrayList<>();

        for (UUID memberId : membersToAdd.getMemberIds()) {
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

    private void validateNotEmpty(TeamMembersToAddInputDto membersToAdd) {
        if (membersToAdd.getMemberIds() == null || membersToAdd.getMemberIds().isEmpty()) {
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
}
