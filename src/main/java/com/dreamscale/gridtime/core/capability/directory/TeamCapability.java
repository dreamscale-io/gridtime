package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamMemberOldDto;
import com.dreamscale.gridtime.core.capability.active.MemberCapability;
import com.dreamscale.gridtime.core.capability.operator.TeamCircuitOperator;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.capability.operator.TorchieNetworkOperator;
import com.dreamscale.gridtime.core.service.GridClock;
import com.dreamscale.gridtime.core.service.MemberDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.dreamscale.exception.ConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class TeamCapability {

    @Autowired
    private TorchieNetworkOperator xpService;

    @Autowired
    private ActiveWorkStatusManager wtfService;

    @Autowired
    private RootAccountRepository rootAccountRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private OrganizationMemberRepository organizationMemberRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private MemberStatusRepository memberStatusRepository;

    @Autowired
    private TeamMemberWorkStatusRepository teamMemberWorkStatusRepository;

    @Autowired
    private TeamMemberHomeRepository teamMemberHomeRepository;

    @Autowired
    private TeamCircuitOperator teamCircuitOperator;

    @Autowired
    private MemberCapability memberCapability;

    @Autowired
    private TeamMemberTombstoneRepository teamMemberTombstoneRepository;

    @Autowired
    private MemberDetailsService memberDetailsService;

    @Autowired
    GridClock gridClock;

    private static final String EVERYONE = "Everyone";
    private static final String LOWER_CASE_EVERYONE = "everyone";

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TeamDto, TeamEntity> teamOutputMapper;

    private DtoEntityMapper<TeamMemberOldDto, TeamMemberEntity> teamMemberOutputMapper;



    @PostConstruct
    private void init() {
        teamOutputMapper = mapperFactory.createDtoEntityMapper(TeamDto.class, TeamEntity.class);
        teamMemberOutputMapper = mapperFactory.createDtoEntityMapper(TeamMemberOldDto.class, TeamMemberEntity.class);
    }

    @Transactional
    public TeamDto createTeam(UUID organizationId, UUID memberId, String teamName) {

        LocalDateTime now = gridClock.now();

        String teamWithNoSpaces = stripSpaces(teamName);
        String standardizedTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamDoesntAlreadyExist(teamWithNoSpaces, teamEntity);

        teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setOrganizationId(organizationId);
        teamEntity.setName(teamWithNoSpaces);
        teamEntity.setLowerCaseName(standardizedTeamName);
        teamEntity.setCreatorId(memberId);
        teamRepository.save(teamEntity);

        TeamMemberEntity teamMember = new TeamMemberEntity();
        teamMember.setId(UUID.randomUUID());
        teamMember.setOrganizationId(organizationId);
        teamMember.setTeamId(teamEntity.getId());
        teamMember.setMemberId(memberId);
        teamMember.setJoinDate(now);

        teamMemberRepository.save(teamMember);

        TeamDto teamDto = teamOutputMapper.toApi(teamEntity);

        teamCircuitOperator.createTeamCircuit(teamDto, memberId);

        updateTeamMemberHomeIfFirstTeam(now, organizationId, memberId, teamEntity.getId());

        return teamDto;
    }

    @Transactional
    public void createEveryoneTeam(UUID organizationId) {

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, LOWER_CASE_EVERYONE);

        validateTeamDoesntAlreadyExist(EVERYONE, teamEntity);

        teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setOrganizationId(organizationId);
        teamEntity.setName(EVERYONE);
        teamEntity.setLowerCaseName(LOWER_CASE_EVERYONE);
        teamRepository.save(teamEntity);

        TeamDto teamDto = teamOutputMapper.toApi(teamEntity);

        teamCircuitOperator.createTeamCircuit(teamDto, null);
    }

    @Transactional
    public void addMemberToEveryone(UUID organizationId, UUID memberId) {

        LocalDateTime now = gridClock.now();

        TeamEntity everyoneTeam = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, LOWER_CASE_EVERYONE);

        validateTeamExists(EVERYONE, everyoneTeam);

        TeamMemberEntity teamMemberEntity = new TeamMemberEntity();
        teamMemberEntity.setId(UUID.randomUUID());
        teamMemberEntity.setOrganizationId(organizationId);
        teamMemberEntity.setTeamId(everyoneTeam.getId());
        teamMemberEntity.setMemberId(memberId);
        teamMemberEntity.setJoinDate(now);

        teamMemberRepository.save(teamMemberEntity);

        teamCircuitOperator.addMemberToTeamCircuit(now, organizationId, everyoneTeam.getId(), memberId);

        TeamMemberHomeEntity memberHome = new TeamMemberHomeEntity();
        memberHome.setId(UUID.randomUUID());
        memberHome.setOrganizationId(organizationId);
        memberHome.setMemberId(memberId);
        memberHome.setHomeTeamId(everyoneTeam.getId());
        memberHome.setLastModifiedDate(gridClock.now());

        teamMemberHomeRepository.save(memberHome);

    }

    private void validateTeamExists(String teamName, TeamEntity teamEntity) {
        if (teamEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Team does not exist: " + teamName);
        }
    }

    private void validateTeamDoesntAlreadyExist(String teamName, TeamEntity teamFound) {
        if (teamFound != null) {
            throw new ConflictException(ConflictErrorCodes.CONFLICTING_TEAM_NAME, "Team already exists by the name: " + teamName);
        }
    }

    private String stripSpaces(String name) {
        return name.replace(" ", "_");
    }

    private String standardizeForSearch(String name) {
        return name.replace(" ", "_").toLowerCase();
    }

    public TeamDto getTeamByName(UUID organizationId, String teamName) {

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, teamName);
        return teamOutputMapper.toApi(teamEntity);
    }


    private void validateMember(UUID memberId, UUID orgId) {
        OrganizationMemberEntity member = organizationMemberRepository.findById(memberId);

        if (member == null || !member.getOrganizationId().equals(orgId)) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT,
                    "Member not found in organization: "+memberId);
        }
    }

    private void validateTeamMemberFound(String teamName, TeamMemberEntity teamMember) {
        if (teamMember == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_IN_TEAM,
                    "Member must be part of the team : "+teamName);
        }

    }

    private void validateTeamFound(String teamName, TeamEntity teamEntity) {
        if (teamEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM,
                    "Team not found : "+teamName);
        }
    }

    private void validateMemberFound(String reference, OrganizationMemberEntity memberEntity) {
        if (memberEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_USERNAME,
                    "Member lookup with '"+reference + "' not found.");
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

    private OrganizationEntity findAndValidateOrganization(UUID orgId) {
        OrganizationEntity orgEntity = organizationRepository.findById(orgId);

        if (orgEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_ORGANIZATION, "Organization not found");
        }

        return orgEntity;
    }

    public List<TeamDto> getAllTeams(UUID orgId) {
        List<TeamEntity> teamEntityList = teamRepository.findByOrganizationId(orgId);
        return teamOutputMapper.toApiList(teamEntityList);
    }


    @Transactional
    public TeamDto getMyActiveTeam(UUID orgId, UUID memberId) {

        //TODO okay, why is this returning null?
        TeamMemberHomeEntity teamMemberHomeConfig = teamMemberHomeRepository.findByOrganizationIdAndMemberId(orgId, memberId);

        log.debug("Home Config?:: "+teamMemberHomeConfig);
        TeamEntity defaultTeam = null;

        if (teamMemberHomeConfig == null) {

            List<TeamEntity> teamEntities = teamRepository.findMyTeamsByOrgMembership(orgId, memberId);

            log.debug("Teams:: {}", teamEntities);
            defaultTeam = chooseDefaultTeam(teamEntities);
            log.debug("DefaultTeam:: {}", defaultTeam);

            if (defaultTeam != null) {
                teamMemberHomeConfig = new TeamMemberHomeEntity();
                teamMemberHomeConfig.setId(UUID.randomUUID());
                teamMemberHomeConfig.setOrganizationId(orgId);
                teamMemberHomeConfig.setMemberId(memberId);
                teamMemberHomeConfig.setHomeTeamId(defaultTeam.getId());

                teamMemberHomeRepository.save(teamMemberHomeConfig);
            }
        } else {

            defaultTeam = teamRepository.findById(teamMemberHomeConfig.getHomeTeamId());
        }

        TeamDto team = teamOutputMapper.toApi(defaultTeam);

        fillTeamWithTeamMembers(team, memberId);

        if (team != null) {
            team.setHomeTeam(true);
        }

        return team;
    }


    private TeamEntity chooseDefaultTeam(List<TeamEntity> teamEntities) {

        TeamEntity defaultTeam = null;

        if (teamEntities.size() == 1) {
            defaultTeam = teamEntities.get(0);
        } else if (teamEntities.size() > 1) {
            for (TeamEntity team : teamEntities) {
                if (!team.getName().equals(EVERYONE)) {
                    defaultTeam = team;
                    break;
                }
            }
        }

        return defaultTeam;
    }

    public List<TeamDto> getMyTeams(UUID orgId, UUID rootAccountId) {
        List<TeamEntity> teamEntityList = teamRepository.findMyTeamsByMembership(orgId, rootAccountId);
        return teamOutputMapper.toApiList(teamEntityList);
    }



    public TeamWithMembersDto getTeam(UUID organizationId, UUID invokingMemberId, String teamName) {

        String standardizeTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizeTeamName);

        validateTeamFound(standardizeTeamName, teamEntity);

        TeamMemberEntity teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamEntity.getId(), invokingMemberId);

        validateTeamMemberFound(standardizeTeamName, teamMember);

        List<TeamMemberDto> memberStatusList = memberCapability.getMembersForMeAndMyTeam(organizationId, invokingMemberId);

        TeamWithMembersDto teamWithMembersDto = new TeamWithMembersDto();
        teamWithMembersDto.setTeamId(teamEntity.getId());
        teamWithMembersDto.setOrganizationId(organizationId);
        teamWithMembersDto.setTeamName(teamEntity.getName());
        teamWithMembersDto.setMe(memberStatusList.get(0));
        if (memberStatusList.size() > 1) {
            teamWithMembersDto.setTeamMembers(memberStatusList.subList(1, memberStatusList.size()));
        } else {
            teamWithMembersDto.setTeamMembers(Collections.emptyList());
        }


        return teamWithMembersDto;
    }

    public TeamDto setMyHomeTeam(UUID organizationId, UUID memberId, String homeTeamName) {

        LocalDateTime now = gridClock.now();

        String standardizedTeamName = standardizeForSearch(homeTeamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        updateTeamMemberHome(now, organizationId, memberId, teamEntity.getId());

        return teamOutputMapper.toApi(teamEntity);
    }

    private void updateTeamMemberHome(LocalDateTime now, UUID organizationId, UUID memberId, UUID teamId) {
        TeamMemberHomeEntity teamMemberHomeConfig = teamMemberHomeRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (teamMemberHomeConfig == null) {
            teamMemberHomeConfig = new TeamMemberHomeEntity();
            teamMemberHomeConfig.setId(UUID.randomUUID());
            teamMemberHomeConfig.setOrganizationId(organizationId);
            teamMemberHomeConfig.setMemberId(memberId);
        }

        teamMemberHomeConfig.setHomeTeamId(teamId);
        teamMemberHomeConfig.setLastModifiedDate(now);

        teamMemberHomeRepository.save(teamMemberHomeConfig);
    }

    private void updateTeamMemberHomeIfFirstTeam(LocalDateTime now, UUID organizationId, UUID memberId, UUID teamId) {
        TeamMemberHomeEntity teamMemberHomeConfig = teamMemberHomeRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        TeamEntity everyoneTeam = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, LOWER_CASE_EVERYONE);

        if (teamMemberHomeConfig == null) {
            teamMemberHomeConfig = new TeamMemberHomeEntity();
            teamMemberHomeConfig.setId(UUID.randomUUID());
            teamMemberHomeConfig.setOrganizationId(organizationId);
            teamMemberHomeConfig.setMemberId(memberId);

            teamMemberHomeConfig.setHomeTeamId(teamId);
            teamMemberHomeConfig.setLastModifiedDate(now);

        } else if (teamMemberHomeConfig.getHomeTeamId().equals(everyoneTeam.getId())){
            teamMemberHomeConfig.setHomeTeamId(teamId);
            teamMemberHomeConfig.setLastModifiedDate(now);
        }

        teamMemberHomeRepository.save(teamMemberHomeConfig);
    }

    public TeamMemberOldDto addMemberToTeam(UUID organizationId, UUID invokingMemberId, String teamName, String userName) {

        String standardizedTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndUsername(organizationId, userName);

        validateMemberFound(userName, memberEntity);

        TeamMemberEntity teamMembership = teamMemberRepository.findByTeamIdAndMemberId(teamEntity.getId(), memberEntity.getId());

        LocalDateTime now = gridClock.now();

        if (teamMembership == null) {
            teamMembership = new TeamMemberEntity();
            teamMembership.setId(UUID.randomUUID());
            teamMembership.setOrganizationId(organizationId);
            teamMembership.setTeamId(teamEntity.getId());
            teamMembership.setMemberId(memberEntity.getId());
            teamMembership.setJoinDate(now);

            teamMemberRepository.save(teamMembership);

            teamCircuitOperator.addMemberToTeamCircuit(now, organizationId, teamEntity.getId(), memberEntity.getId());

        }
        return toDto(userName, teamMembership);
    }

    public SimpleStatusDto inviteUserToMyActiveTeam(UUID organizationId, UUID invokingMemberId, String userToInvite) {

        TeamDto activeTeam = getMyActiveTeam(organizationId, invokingMemberId);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndUsername(organizationId, standardizeForSearch(userToInvite));

        validateMemberFound(userToInvite, membership);

        addMemberToTeamWithMemberId(organizationId, invokingMemberId, activeTeam.getName(), membership.getId());

        return new SimpleStatusDto(Status.JOINED, "Added member to team.");
    }

    public TeamMemberOldDto addMemberToTeamWithMemberId(UUID organizationId, UUID invokingMemberId, String teamName, UUID memberId) {
        String standardizedTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        LocalDateTime now = gridClock.now();

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndId(organizationId, memberId);
        validateMemberFound(memberId.toString(), membership);

        return joinTeam(now, organizationId, membership, teamEntity);
    }

    private TeamMemberOldDto joinTeam(LocalDateTime now, UUID organizationId, OrganizationMemberEntity memberEntity, TeamEntity team) {

        TeamMemberEntity teamMembership = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), memberEntity.getId());

        if (teamMembership == null) {
            log.debug("Adding member {} to team {}", memberEntity.getEmail(), team.getName());

            teamMembership = new TeamMemberEntity();
            teamMembership.setId(UUID.randomUUID());
            teamMembership.setOrganizationId(organizationId);
            teamMembership.setTeamId(team.getId());
            teamMembership.setMemberId(memberEntity.getId());
            teamMembership.setJoinDate(now);

            teamMemberRepository.save(teamMembership);

            teamCircuitOperator.addMemberToTeamCircuit(now, organizationId, team.getId(), memberEntity.getId());

            updateTeamMemberHomeIfFirstTeam(now, organizationId, teamMembership.getMemberId(), team.getId());

        } else {
            log.warn("Member {} already added to team {}", memberEntity.getEmail(), team.getName());
        }
        return toDto(memberEntity.getUsername(), teamMembership);
    }

    public SimpleStatusDto joinTeam(LocalDateTime now, UUID rootAccountId, UUID organizationId, UUID teamId) {

        OrganizationMemberEntity member = organizationMemberRepository.findByOrganizationIdAndRootAccountId(organizationId, rootAccountId);

        validateMemberAlreadyInOrg(member);

        TeamEntity team = teamRepository.findById(teamId);

        TeamMemberOldDto teamMember = joinTeam(now, organizationId, member, team);

        SimpleStatusDto status = new SimpleStatusDto();
        status.setStatus(Status.JOINED);
        status.setMessage("Member joined "+team.getName() + ".");

        return status;
    }

    private void validateMemberAlreadyInOrg(OrganizationMemberEntity member) {
        if (member == null) {
            throw new BadRequestException(ValidationErrorCodes.NO_ORG_MEMBERSHIP_FOR_ACCOUNT, "Membership for user not found, please join the organization first.");
        }
    }

    @Transactional
    public TeamMemberOldDto removeMemberFromTeam(UUID organizationId, UUID invokingMemberId, String teamName, String userName) {

        String standardizedTeamName = standardizeForSearch(teamName);

        LocalDateTime now = gridClock.now();

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndUsername(organizationId, userName);

        validateMemberFound(userName, memberEntity);


        return removeTeamMemberAndCreateTombstone(now, organizationId, teamEntity, memberEntity.getId());
    }

    public TeamMemberOldDto removeMemberFromTeamWithMemberId(UUID organizationId, UUID invokingMemberId, String teamName, UUID memberId) {

        String standardizedTeamName = standardizeForSearch(teamName);

        LocalDateTime now = gridClock.now();

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndId(organizationId, memberId);

        validateMemberFound(memberId.toString(), memberEntity);

        return removeTeamMemberAndCreateTombstone(now, organizationId, teamEntity, memberEntity.getId());
    }

    public void removeMemberFromAllTeams(LocalDateTime now, UUID organizationId, UUID memberId) {

        List<TeamEntity> teamEntities = teamRepository.findMyTeamsByOrgMembership(organizationId, memberId);

        for (TeamEntity team: teamEntities) {
            removeTeamMemberAndCreateTombstone(now, organizationId, team, memberId);
        }

    }

    private TeamMemberOldDto removeTeamMemberAndCreateTombstone(LocalDateTime now, UUID organizationId, TeamEntity teamEntity, UUID memberId) {

        TeamMemberEntity teamMembership = teamMemberRepository.findByTeamIdAndMemberId(teamEntity.getId(), memberId);

        String memberUserName = "[member]";

        if (teamMembership != null) {

            TeamMemberTombstoneEntity teamMemberTombstoneEntity = new TeamMemberTombstoneEntity();
            teamMemberTombstoneEntity.setId(UUID.randomUUID());
            teamMemberTombstoneEntity.setTeamId(teamMembership.getTeamId());
            teamMemberTombstoneEntity.setOrganizationId(teamMembership.getOrganizationId());
            teamMemberTombstoneEntity.setMemberId(teamMembership.getMemberId());
            teamMemberTombstoneEntity.setJoinDate(teamMembership.getJoinDate());
            teamMemberTombstoneEntity.setRipDate(now);

            MemberDetailsEntity memberDetails = memberDetailsService.lookupMemberDetails(teamMembership.getOrganizationId(), teamMembership.getMemberId());

            memberUserName = memberDetails.getUsername();

            teamMemberTombstoneEntity.setEmail(memberDetails.getEmail());
            teamMemberTombstoneEntity.setUsername(memberDetails.getUsername());
            teamMemberTombstoneEntity.setDisplayName(memberDetails.getDisplayName());
            teamMemberTombstoneEntity.setFullName(memberDetails.getFullName());

            teamMemberTombstoneRepository.save(teamMemberTombstoneEntity);

            teamMemberRepository.delete(teamMembership);

            teamCircuitOperator.removeMemberFromTeamCircuit(organizationId, teamEntity.getId(), memberId);

        }
        return toDto(memberUserName, teamMembership);
    }

    private TeamMemberOldDto toDto(String userName, TeamMemberEntity teamMembership) {
        TeamMemberOldDto teamMemberDto = teamMemberOutputMapper.toApi(teamMembership);

        teamMemberDto.setUserName(userName);

        return teamMemberDto;
    }

    public List<TeamDto> getAllMyParticipatingTeamsWithMembers(UUID organizationId, UUID invokingMemberId) {

        List<TeamEntity> teams = teamRepository.findMyTeamsByOrgMembership(organizationId, invokingMemberId);

        List<TeamDto> teamDtos = teamOutputMapper.toApiList(teams);

        TeamDto homeTeam = getMyActiveTeam(organizationId, invokingMemberId);

        List<TeamDto> sortedTeams = sortTeams(homeTeam, null, teamDtos);

        fillTeamDtosWithTeamMembers(sortedTeams, invokingMemberId);

        return sortedTeams;
    }

    public List<TeamDto> getMyParticipatingTeamsWithoutMembers(UUID organizationId, UUID invokingMemberId) {

        List<TeamEntity> teams = teamRepository.findMyTeamsByOrgMembership(organizationId, invokingMemberId);

        List<TeamDto> teamDtos = teamOutputMapper.toApiList(teams);

        TeamDto homeTeam = getMyActiveTeam(organizationId, invokingMemberId);
        TeamDto everyoneTeam = findEveryoneTeam(teamDtos);

        return sortTeams(homeTeam, everyoneTeam, teamDtos);
    }

    private void fillTeamDtosWithTeamMembers(List<TeamDto> sortedTeams, UUID meId) {
        for (TeamDto team : sortedTeams) {

            fillTeamWithTeamMembers(team, meId);
        }
    }

    private void fillTeamWithTeamMembers(TeamDto team, UUID meId) {
        if (team != null) {
            log.debug("Team member retrieval, team: {}", team);
            List<TeamMemberDto> membersWithDetails = memberCapability.getMembersForTeam(team.getOrganizationId(), team.getId(), meId);
            team.setTeamMembers(membersWithDetails);
        }
    }


    private TeamDto findEveryoneTeam(List<TeamDto> teamDtos) {

        TeamDto everyoneTeam = null;

        for (TeamDto teamDto : teamDtos) {
            if (teamDto.getName().equals(LOWER_CASE_EVERYONE)) {
                everyoneTeam = teamDto;
                break;
            }
        }

        return everyoneTeam;
    }

    private List<TeamDto> sortTeams(TeamDto homeTeam, TeamDto everyoneTeam, List<TeamDto> teamDtos) {

        List<TeamDto> sortedTeamList = new ArrayList<>();

        sortedTeamList.add(homeTeam);

        if (everyoneTeam != null && !isSame(homeTeam, everyoneTeam)) {
            sortedTeamList.add(everyoneTeam);
        }

        for (TeamDto teamDto : teamDtos) {
            if (!isSame(teamDto, homeTeam) && !isSame(teamDto, everyoneTeam)) {
                sortedTeamList.add(teamDto);
            }
        }

        return sortedTeamList;
    }

    private boolean isSame(TeamDto team1, TeamDto team2) {
        return (team1 != null && team2 != null && team1.getId().equals(team2.getId()));
    }



}
