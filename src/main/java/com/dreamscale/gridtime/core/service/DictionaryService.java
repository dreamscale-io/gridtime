package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.dictionary.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitEntity;
import com.dreamscale.gridtime.core.domain.dictionary.TeamDictionaryTagEntity;
import com.dreamscale.gridtime.core.domain.dictionary.TeamDictionaryTagRepository;
import com.dreamscale.gridtime.core.domain.dictionary.TeamDictionaryTombstoneEntity;
import com.dreamscale.gridtime.core.domain.dictionary.TeamDictionaryTombstoneRepository;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DictionaryService {


    @Autowired
    TimeService timeService;

    @Autowired
    TeamService teamService;

    @Autowired
    TeamDictionaryTagRepository teamDictionaryTagRepository;

    @Autowired
    TeamDictionaryTombstoneRepository teamDictionaryTombstoneRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TagDefinitionWithDetailsDto, TeamDictionaryTagEntity> tagDefinitionMapper;

    @PostConstruct
    private void init() {
        tagDefinitionMapper = mapperFactory.createDtoEntityMapper(TagDefinitionWithDetailsDto.class, TeamDictionaryTagEntity.class);
    }

    public TagDefinitionWithDetailsDto getDefinition(UUID organizationId, UUID memberId, String tagName) {

        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        LocalDateTime now = timeService.now();

        //get all definitions, across all scopes, and then all tombstone references that are forwarded here

        TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndTagName(myTeam.getId(), tagName);

        TagDefinitionWithDetailsDto tagDefinition = tagDefinitionMapper.toApi(existingTag);;

        if (existingTag != null) {

            List<TeamDictionaryTombstoneEntity> tombstones = teamDictionaryTombstoneRepository.findByForwardToOrderByRipDate(existingTag.getId());

            List<TagTombstoneDto> tombstoneDtos = new ArrayList<>();

            for (TeamDictionaryTombstoneEntity tombstone: tombstones) {
                TagTombstoneDto tombstoneDto = new TagTombstoneDto();
                tombstoneDto.setDeadTagName(tombstone.getDeadTagName());
                tombstoneDto.setDeadDefinition(tombstone.getDeadDefinition());
                tombstoneDto.setRipDate(tombstone.getRipDate());

                tombstoneDtos.add(tombstoneDto);
            }

            tagDefinition.setTombstones(tombstoneDtos);
        }

        return tagDefinition;
    }

    public TagDefinitionDto refactorDefinition(UUID organizationId, UUID memberId, String originalTagName, TagRefactorInputDto tagRefactorInputDto) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndTagName(myTeam.getId(), originalTagName);

        LocalDateTime now = timeService.now();

        TeamDictionaryTagEntity updatedTag = null;
        if (existingTag != null) {
            updatedTag = refactorExistingTag(now, existingTag, tagRefactorInputDto);
        } else {
            updatedTag = createNewTag(now, myTeam, tagRefactorInputDto);
        }

        return toDto(updatedTag);
    }

    private TagDefinitionDto toDto(TeamDictionaryTagEntity dictionaryTag) {

        TagDefinitionDto tagDefinitionDto = new TagDefinitionDto();

        tagDefinitionDto.setId(dictionaryTag.getId());
        tagDefinitionDto.setTagName(dictionaryTag.getTagName());
        tagDefinitionDto.setDefinition(dictionaryTag.getDefinition());

        return tagDefinitionDto;
    }

    private TeamDictionaryTagEntity createNewTag(LocalDateTime now, TeamDto myTeam, TagRefactorInputDto tagRefactorInputDto) {
        TeamDictionaryTagEntity newTag = new TeamDictionaryTagEntity();
        newTag.setOrganizationId(myTeam.getOrganizationId());
        newTag.setTeamId(myTeam.getId());
        newTag.setCreationDate(now);
        newTag.setLastModifiedDate(now);
        newTag.setTagName(tagRefactorInputDto.getTagName());
        newTag.setDefinition(tagRefactorInputDto.getDefinition());

        teamDictionaryTagRepository.save(newTag);

        return newTag;
    }

    private TeamDictionaryTagEntity refactorExistingTag(LocalDateTime now, TeamDictionaryTagEntity existingTag, TagRefactorInputDto tagRefactorInputDto) {

        if (existingTag.getTagName().equals(tagRefactorInputDto.getTagName())) {
            //definition change only, just update it

            existingTag.setDefinition(tagRefactorInputDto.getDefinition());
            existingTag.setLastModifiedDate(now);

            teamDictionaryTagRepository.save(existingTag);
        } else {
            //name change, create a tombstone link

            TeamDictionaryTombstoneEntity teamDictionaryTombstoneEntity = new TeamDictionaryTombstoneEntity();

            teamDictionaryTombstoneEntity.setId(UUID.randomUUID());
            teamDictionaryTombstoneEntity.setDeadTagName(existingTag.getTagName());
            teamDictionaryTombstoneEntity.setDeadDefinition(existingTag.getDefinition());

            teamDictionaryTombstoneEntity.setRipDate(now);
            teamDictionaryTombstoneEntity.setForwardTo(existingTag.getId());

            teamDictionaryTombstoneRepository.save(teamDictionaryTombstoneEntity);

            //then edit the original

            existingTag.setTagName(tagRefactorInputDto.getTagName());
            existingTag.setDefinition(tagRefactorInputDto.getDefinition());
            existingTag.setLastModifiedDate(now);

            teamDictionaryTagRepository.save(existingTag);

        }

        return existingTag;
    }

    public void touchBlankDefinition(UUID organizationId, UUID memberId, String tag) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndTagName(myTeam.getId(), tag);

        LocalDateTime now = timeService.now();

        if (existingTag == null) {
            TeamDictionaryTagEntity newTag = new TeamDictionaryTagEntity();
            newTag.setOrganizationId(organizationId);
            newTag.setTeamId(myTeam.getId());
            newTag.setCreationDate(now);
            newTag.setLastModifiedDate(now);
            newTag.setTagName(tag);

            teamDictionaryTagRepository.save(newTag);
        }
    }

    public void touchBlankDefinitions(UUID organizationId, UUID memberId, List<String> tags) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        LocalDateTime now = timeService.now();

        List<TeamDictionaryTagEntity> newTagEntries = new ArrayList<>();

        for (String tag : tags) {
            TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndTagName(myTeam.getId(), tag);

            if (existingTag == null) {
                TeamDictionaryTagEntity newTag = new TeamDictionaryTagEntity();
                newTag.setId(UUID.randomUUID());
                newTag.setOrganizationId(organizationId);
                newTag.setTeamId(myTeam.getId());
                newTag.setCreationDate(now);
                newTag.setLastModifiedDate(now);
                newTag.setTagName(tag);

                newTagEntries.add(newTag);
            }
        }

        if (newTagEntries.size() > 0) {
            teamDictionaryTagRepository.save(newTagEntries);
        }
    }

    private void validateTeamExists(TeamDto myTeam) {
        if (myTeam == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Unable to find team for member.");
        }
    }

    private void validateCircuitExists(String circuitName, LearningCircuitEntity learningCircuitEntity) {
        if (learningCircuitEntity == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_CIRCUIT, "Unable to find: " + circuitName);
        }
    }



    public TagDefinitionDto promoteDefinition(UUID organizationId, UUID id, String tagName) {
        return null;
    }


    public List<TagDefinitionDto> getTeamDictionary(UUID organizationId, UUID memberId) {
        return null;
    }

    public List<TagDefinitionDto> getCommunityDictionary(UUID organizationId, UUID memberId) {
        return null;
    }

    public List<PendingTagReferenceDto> getPendingCommunityDefinitions(UUID organizationId, UUID id) {
        return null;
    }

    public List<TagDefinitionDto> getUndefinedTeamDictionaryTerms(UUID organizationId, UUID id) {
        return null;
    }

    public List<TagDefinitionDto> getPendingTeamDictionaryTerms(UUID organizationId, UUID id) {
        return null;
    }

    public TagDefinitionDto acceptPendingTagIntoDictionary(UUID organizationId, UUID id, PendingTagReferenceDto pendingTagReferenceDto) {
        return null;

    }

    public TagDefinitionDto pullDefinitionIntoTeamBook(UUID organizationId, UUID id, String bookName, String tagName) {
        return null;
    }

    public void rejectPendingTag(UUID organizationId, UUID id, PendingTagReferenceDto pendingTagReferenceDto) {

    }

    public DictionaryBookDto createTeamDictionaryBook(UUID organizationId, UUID id, String bookName) {
        return null;
    }

    public DictionaryBookDto createCommunityDictionaryBook(UUID organizationId, UUID id, String bookName) {
        return null;
    }

    public List<TagDefinitionDto> getTeamBook(UUID organizationId, UUID id, String bookName) {
        return null;
    }

    public List<TagDefinitionDto> getCommunityBook(UUID organizationId, UUID id, String bookName) {
        return null;
    }

    public TagDefinitionDto getDefinitionWithinTeamBook(UUID organizationId, UUID id, String bookName, String tagName) {
        return null;
    }

    public TagDefinitionDto pullDefinitionIntoCommunityBook(UUID organizationId, UUID id, String bookName, String tagName) {
        return null;
    }

    public TagDefinitionDto refactorTeamBookDefinition(UUID organizationId, UUID id, String bookName, String tagName, TagRefactorInputDto tagRefactorInputDto) {
        return null;
    }
}
