package com.dreamscale.gridtime.core.capability.membership;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import com.dreamscale.gridtime.api.organization.*;
import com.dreamscale.gridtime.api.status.Status;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.api.team.TeamLinkDto;
import com.dreamscale.gridtime.api.team.TeamMemberOldDto;
import com.dreamscale.gridtime.core.capability.active.MemberDetailsRetriever;
import com.dreamscale.gridtime.core.capability.active.MemberStatusManager;
import com.dreamscale.gridtime.core.capability.circuit.TeamCircuitOperator;
import com.dreamscale.gridtime.core.domain.member.*;
import com.dreamscale.gridtime.core.exception.ConflictErrorCodes;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.capability.active.ActiveWorkStatusManager;
import com.dreamscale.gridtime.core.capability.circuit.TorchieNetworkOperator;
import com.dreamscale.gridtime.core.capability.system.GridClock;
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
    private MemberStatusManager memberStatusManager;

    @Autowired
    private TeamMemberTombstoneRepository teamMemberTombstoneRepository;

    @Autowired
    private MemberDetailsRetriever memberDetailsRetriever;

    @Autowired
    GridClock gridClock;

    private static final String EVERYONE = "Everyone";
    private static final String LOWER_CASE_EVERYONE = "everyone";

    private static final String ME_TEAM_PREFIX = "me-";


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TeamDto, TeamEntity> teamOutputMapper;
    private DtoEntityMapper<TeamLinkDto, TeamEntity> teamLinkOutputMapper;

    private DtoEntityMapper<TeamMemberOldDto, TeamMemberEntity> teamMemberOutputMapper;



    @PostConstruct
    private void init() {
        teamOutputMapper = mapperFactory.createDtoEntityMapper(TeamDto.class, TeamEntity.class);
        teamMemberOutputMapper = mapperFactory.createDtoEntityMapper(TeamMemberOldDto.class, TeamMemberEntity.class);

        teamLinkOutputMapper =  mapperFactory.createDtoEntityMapper(TeamLinkDto.class, TeamEntity.class);
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
        teamEntity.setTeamType(TeamType.OPEN);
        teamRepository.save(teamEntity);

        TeamMemberEntity teamMember = new TeamMemberEntity();
        teamMember.setId(UUID.randomUUID());
        teamMember.setOrganizationId(organizationId);
        teamMember.setTeamId(teamEntity.getId());
        teamMember.setMemberId(memberId);
        teamMember.setJoinDate(now);

        teamMemberRepository.save(teamMember);

        TeamDto teamDto = teamOutputMapper.toApi(teamEntity);

        teamCircuitOperator.createTeamCircuit(now, teamDto, memberId);

        UUID homeTeamId = updateTeamMemberHomeIfFirstTeam(now, organizationId, memberId, teamEntity.getId());

        if (homeTeamId.equals(teamDto.getId())) {
            teamDto.setHomeTeam(true);
        }

        fillTeamWithTeamMembers(teamDto, memberId);

        return teamDto;
    }

    @Transactional
    public void createEveryoneTeam(LocalDateTime now, UUID organizationId) {

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, LOWER_CASE_EVERYONE);

        validateTeamDoesntAlreadyExist(EVERYONE, teamEntity);

        teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setOrganizationId(organizationId);
        teamEntity.setName(EVERYONE);
        teamEntity.setLowerCaseName(LOWER_CASE_EVERYONE);
        teamEntity.setTeamType(TeamType.EVERYONE);
        teamRepository.save(teamEntity);

        TeamDto teamDto = teamOutputMapper.toApi(teamEntity);

        teamCircuitOperator.createTeamCircuit(now, teamDto, null);
    }

    public void createMeTeam(LocalDateTime now, UUID organizationId, UUID memberId) {

        TeamEntity teamEntity = new TeamEntity();
        teamEntity.setId(UUID.randomUUID());
        teamEntity.setOrganizationId(organizationId);
        teamEntity.setName(ME_TEAM_PREFIX + memberId);
        teamEntity.setLowerCaseName(ME_TEAM_PREFIX + memberId);
        teamEntity.setTeamType(TeamType.ME);
        teamRepository.save(teamEntity);

        TeamMemberEntity teamMemberEntity = new TeamMemberEntity();
        teamMemberEntity.setId(UUID.randomUUID());
        teamMemberEntity.setOrganizationId(organizationId);
        teamMemberEntity.setTeamId(teamEntity.getId());
        teamMemberEntity.setMemberId(memberId);
        teamMemberEntity.setJoinDate(now);

        teamMemberRepository.save(teamMemberEntity);

        updateTeamMemberHomeIfFirstTeam(now, organizationId, memberId, teamEntity.getId());
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

        updateTeamMemberHomeIfFirstTeam(now, organizationId, memberId, everyoneTeam.getId());

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

    @Deprecated
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

    private void validateMemberIsOnTeam(String teamName, TeamMemberEntity teamMember) {
        if (teamMember == null) {
            throw new BadRequestException(ValidationErrorCodes.MEMBER_NOT_IN_TEAM,
                    "Member must be on the team to perform this operation: "+teamName);
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

    public List<TeamLinkDto> getLinksToAllOpenTeams(UUID orgId) {
        log.debug("getLinksToAllOpenTeams : "+ orgId);

        List<TeamEntity> teamEntityList = teamRepository.findOpenTeamsByOrganizationId(orgId);

        log.debug("results : "+ teamEntityList);

        return teamLinkOutputMapper.toApiList(teamEntityList);
    }

    public TeamLinkDto getMyActiveTeamLink(UUID organizationId, UUID memberId) {
        TeamEntity defaultTeam = getMyActiveTeamEntity(organizationId, memberId);

        return teamLinkOutputMapper.toApi(defaultTeam);
    }

    public TeamDto getMyActiveTeam(UUID orgId, UUID memberId) {

        //TODO okay, why is this returning null?
        TeamEntity defaultTeam = getMyActiveTeamEntity(orgId, memberId);

        TeamDto team = teamOutputMapper.toApi(defaultTeam);

        fillTeamWithTeamMembers(team, memberId);

        if (team != null) {
            team.setHomeTeam(true);
        }

        return team;
    }

    @Transactional
    TeamEntity getMyActiveTeamEntity(UUID orgId, UUID memberId) {
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
        return defaultTeam;
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

    public TeamDto getTeam(UUID organizationId, UUID invokingMemberId, String teamName) {

        String standardizeTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizeTeamName);

        validateTeamFound(standardizeTeamName, teamEntity);

        TeamMemberEntity teamMember = teamMemberRepository.findByTeamIdAndMemberId(teamEntity.getId(), invokingMemberId);

        validateMemberIsOnTeam(standardizeTeamName, teamMember);

        TeamDto teamDto = teamOutputMapper.toApi(teamEntity);

        fillTeamWithTeamMembers(teamDto, invokingMemberId);


        return teamDto;
    }

    public SimpleStatusDto setMyHomeTeam(UUID organizationId, UUID memberId, String homeTeamName) {

        LocalDateTime now = gridClock.now();

        String standardizedTeamName = standardizeForSearch(homeTeamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        updateTeamMemberHome(now, organizationId, memberId, teamEntity.getId());

        return new SimpleStatusDto(Status.SUCCESS, "Updated home team to "+homeTeamName);
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

    private UUID updateTeamMemberHomeIfFirstTeam(LocalDateTime now, UUID organizationId, UUID memberId, UUID teamId) {
        TeamMemberHomeEntity teamMemberHomeConfig = teamMemberHomeRepository.findByOrganizationIdAndMemberId(organizationId, memberId);

        if (teamMemberHomeConfig != null) {
            TeamEntity currentHomeTeam = teamRepository.findById(teamMemberHomeConfig.getHomeTeamId());

            if (currentHomeTeam.getTeamType() == TeamType.ME || currentHomeTeam.getTeamType() == TeamType.EVERYONE) {
                teamMemberHomeConfig.setHomeTeamId(teamId);
                teamMemberHomeConfig.setLastModifiedDate(now);
            }
        } else {
            teamMemberHomeConfig = new TeamMemberHomeEntity();
            teamMemberHomeConfig.setId(UUID.randomUUID());
            teamMemberHomeConfig.setOrganizationId(organizationId);
            teamMemberHomeConfig.setMemberId(memberId);
            teamMemberHomeConfig.setHomeTeamId(teamId);
            teamMemberHomeConfig.setLastModifiedDate(now);
        }

        teamMemberHomeRepository.save(teamMemberHomeConfig);

        return teamMemberHomeConfig.getHomeTeamId();
    }



    public TeamMemberOldDto addMemberToTeam(UUID organizationId, UUID invokingMemberId, String teamName, String username) {

        String standardizedTeamName = standardizeForSearch(teamName);

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, username);

        validateMemberFound(username, memberEntity);

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
        return toDto(username, teamMembership);
    }

    public SimpleStatusDto inviteUserToMyActiveTeam(UUID organizationId, UUID invokingMemberId, String userToInvite) {

        TeamDto activeTeam = getMyActiveTeam(organizationId, invokingMemberId);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, standardizeForSearch(userToInvite));

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

    public SimpleStatusDto joinTeam(UUID organizationId, UUID memberId, String teamName) {

        String standardizeTeamName = standardizeForSearch(teamName);

        TeamEntity team = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizeTeamName);

        validateTeamFound(standardizeTeamName, team);
        validateTeamIsOpen(standardizeTeamName, team);

        OrganizationMemberEntity membership = organizationMemberRepository.findByOrganizationIdAndId(organizationId, memberId);

        LocalDateTime now = gridClock.now();

        TeamMemberEntity teamMembership = teamMemberRepository.findByTeamIdAndMemberId(team.getId(), memberId);

        if (teamMembership == null) {
            log.debug("Member {} is joining team {}", membership.getBestAvailableName(), team.getName());

            teamMembership = new TeamMemberEntity();
            teamMembership.setId(UUID.randomUUID());
            teamMembership.setOrganizationId(organizationId);
            teamMembership.setTeamId(team.getId());
            teamMembership.setMemberId(membership.getId());
            teamMembership.setJoinDate(now);

            teamMemberRepository.save(teamMembership);

            teamCircuitOperator.addMemberToTeamCircuit(now, organizationId, team.getId(), membership.getId());

            updateTeamMemberHomeIfFirstTeam(now, organizationId, teamMembership.getMemberId(), team.getId());

        } else {
            log.warn("Member {} is already member of team {}", membership.getBestAvailableName(), team.getName());
        }

        SimpleStatusDto status = new SimpleStatusDto();
        status.setStatus(Status.JOINED);
        status.setMessage("Member joined "+team.getName() + ".");

        return status;
    }

    private void validateTeamIsOpen(String standardizeTeamName, TeamEntity team) {
        if (team.getTeamType() != TeamType.OPEN) {
            throw new BadRequestException(ValidationErrorCodes.TEAM_MUST_BE_OPEN_TO_JOIN,
                    "Team "+standardizeTeamName + " must be open to to join.");
        }
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
    public TeamMemberOldDto removeMemberFromTeam(UUID organizationId, UUID invokingMemberId, String teamName, String username) {

        String standardizedTeamName = standardizeForSearch(teamName);
        String standardizedUsername = standardizeForSearch(username);

        LocalDateTime now = gridClock.now();

        TeamEntity teamEntity = teamRepository.findByOrganizationIdAndLowerCaseName(organizationId, standardizedTeamName);

        validateTeamFound(standardizedTeamName, teamEntity);

        teamCircuitOperator.validateMemberIsOwnerOrModeratorOfTeam(organizationId, teamEntity.getId(), invokingMemberId);

        OrganizationMemberEntity memberEntity = organizationMemberRepository.findByOrganizationIdAndLowercaseUsername(organizationId, standardizedUsername);

        validateMemberFound(username, memberEntity);

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

            MemberDetailsEntity memberDetails = memberDetailsRetriever.lookupMemberDetails(teamMembership.getOrganizationId(), teamMembership.getMemberId());

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

    private TeamMemberOldDto toDto(String username, TeamMemberEntity teamMembership) {
        TeamMemberOldDto teamMemberDto = teamMemberOutputMapper.toApi(teamMembership);

        teamMemberDto.setUsername(username);

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
            List<TeamMemberDto> membersWithDetails = memberStatusManager.getMembersForTeam(team.getOrganizationId(), team.getId(), meId);
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
