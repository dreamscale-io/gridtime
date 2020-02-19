package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.dictionary.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.circuit.LearningCircuitEntity;
import com.dreamscale.gridtime.core.domain.dictionary.*;
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
    TeamBookRepository teamBookRepository;

    @Autowired
    TeamBookTagRepository teamBookTagRepository;

    @Autowired
    TeamBookOverrideRepository teamBookOverrideRepository;


    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TagDefinitionWithDetailsDto, TeamDictionaryTagEntity> tagDefinitionWithDetailsMapper;
    private DtoEntityMapper<BookReferenceDto, TeamBookEntity> teamBookReferenceMapper;
    private DtoEntityMapper<BookDto, TeamBookEntity> teamBookMapper;
    private DtoEntityMapper<TagDefinitionDto, TeamDictionaryTagEntity> tagDefinitionMapper;
    private DtoEntityMapper<TagDefinitionDto, TeamBookOverrideEntity> tagOverrideMapper;


    @PostConstruct
    private void init() {
        tagDefinitionMapper = mapperFactory.createDtoEntityMapper(TagDefinitionDto.class, TeamDictionaryTagEntity.class);
        tagOverrideMapper = mapperFactory.createDtoEntityMapper(TagDefinitionDto.class, TeamBookOverrideEntity.class);

        tagDefinitionWithDetailsMapper = mapperFactory.createDtoEntityMapper(TagDefinitionWithDetailsDto.class, TeamDictionaryTagEntity.class);
        teamBookReferenceMapper = mapperFactory.createDtoEntityMapper(BookReferenceDto.class, TeamBookEntity.class );
        teamBookMapper = mapperFactory.createDtoEntityMapper(BookDto.class, TeamBookEntity.class);
    }


    public TagDefinitionWithDetailsDto getDefinition(UUID organizationId, UUID memberId, String tagName) {

        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        LocalDateTime now = timeService.now();

        //get all definitions, across all scopes, and then all tombstone references that are forwarded here

        TeamDictionaryTagEntity existingTag = findByTeamAndCaseInsensitiveTag(myTeam.getId(), tagName);

        TagDefinitionWithDetailsDto tagDefinition = tagDefinitionWithDetailsMapper.toApi(existingTag);

        if (existingTag != null) {

            List<TeamDictionaryTombstoneEntity> tombstones = teamDictionaryTombstoneRepository.findByForwardToOrderByRipDate(existingTag.getId());

            List<TagTombstoneDto> tombstoneDtos = new ArrayList<>();

            for (TeamDictionaryTombstoneEntity tombstone: tombstones) {
                TagTombstoneDto tombstoneDto = new TagTombstoneDto();
                tombstoneDto.setDeadTagName(tombstone.getDeadTagName());
                tombstoneDto.setDeadDefinition(tombstone.getDeadDefinition());
                tombstoneDto.setRipDate(tombstone.getRipDate());
                tombstoneDto.setReviveDate(tombstone.getReviveDate());

                tombstoneDtos.add(tombstoneDto);
            }

            tagDefinition.setTombstones(tombstoneDtos);
        }

        return tagDefinition;
    }

    private void validateBookNotNull(String bookName) {
        if (bookName == null || bookName.length() == 0) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_BOOK, "Book name cant be blank.");
        }
    }

    private void validateBookNotNull(TeamBookEntity book) {
        if (book == null ) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_BOOK, "Book not found.");
        }
    }

    private void validateTagNotNull(String tagName) {
        if (tagName == null || tagName.length() == 0) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TAG, "Tag name cant be blank.");
        }
    }

    private void validateTagNotNull(TeamDictionaryTagEntity tagEntity) {
        if (tagEntity == null ) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TAG, "Tag could not be found.");
        }
    }

    private void validatBookNotNull(TeamBookEntity book) {
        if (book == null ) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_BOOK, "Book not found");
        }
    }


    private TeamDictionaryTagEntity findByTeamAndCaseInsensitiveTag(UUID teamId, String tagName) {

        String lowerCaseTag = tagName.toLowerCase();

        return teamDictionaryTagRepository.findByTeamIdAndLowerCaseTagName(teamId, lowerCaseTag);
    }

    public TagDefinitionDto createOrRefactorDefinition(UUID organizationId, UUID memberId, String originalTagName, TagDefinitionInputDto tagDefinitionInputDto) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateTagNotNull(originalTagName);
        validateTagNotNull(tagDefinitionInputDto.getTagName());

        TeamDictionaryTagEntity existingTag = findByTeamAndCaseInsensitiveTag(myTeam.getId(), originalTagName);

        LocalDateTime now = timeService.now();

        TeamDictionaryTagEntity updatedTag = null;
        if (existingTag != null) {
            updatedTag = refactorExistingTag(now, existingTag, tagDefinitionInputDto);
        } else {
            updatedTag = createNewTag(now, myTeam, tagDefinitionInputDto);
        }

        //what happens if an existing tag, is a tombstone?

        //what happens if a tombstone is revived?


        return toDto(updatedTag);
    }

    public BookReferenceDto createTeamBook(UUID organizationId, UUID memberId, String bookName) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName);

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        if (existingBook == null) {
            //create a new book

            TeamBookEntity bookEntity = new TeamBookEntity();
            bookEntity.setId(UUID.randomUUID());
            bookEntity.setBookName(bookName);
            bookEntity.setLowerCaseBookName(bookName.toLowerCase());
            bookEntity.setOrganizationId(organizationId);
            bookEntity.setTeamId(myTeam.getId());
            bookEntity.setCreationDate(now);
            bookEntity.setLastModifiedDate(now);

            teamBookRepository.save(bookEntity);

            existingBook = bookEntity;

        }

        return teamBookReferenceMapper.toApi(existingBook);
    }

    public TagDefinitionDto pullDefinitionIntoTeamBook(UUID organizationId, UUID memberId, String bookName, String tagName) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);
        validateTagNotNull(tagName);

        TeamDictionaryTagEntity tag = findByTeamAndCaseInsensitiveTag(myTeam.getId(), tagName);

        validateTagNotNull(tag);

        TeamBookTagEntity existingTag = pullTagInsideBook(myTeam.getId(), bookName, tag.getId());

        return tagDefinitionMapper.toApi(tag);
    }

    private TeamBookTagEntity pullTagInsideBook(UUID teamId, String bookName, UUID tagId) {

        String lowerCaseBook = bookName.toLowerCase();

        TeamBookEntity book = teamBookRepository.findByTeamIdAndLowerCaseBookName(teamId, bookName.toLowerCase());

        validateBookNotNull(book);

        TeamBookTagEntity bookTag = teamBookTagRepository.findByTeamBookNameAndTagId(teamId, lowerCaseBook, tagId);

        if (bookTag == null) {
            bookTag = new TeamBookTagEntity();
            bookTag.setId(UUID.randomUUID());
            bookTag.setTeamBookId(book.getId());
            bookTag.setTeamTagId(tagId);
            bookTag.setModifiedStatus(TagModifiedStatus.UNCHANGED);
            bookTag.setPullDate(timeService.now());

            teamBookTagRepository.save(bookTag);
        }

        return bookTag;
    }

    public BookDto getTeamBook(UUID organizationId, UUID memberId, String bookName) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName);

        validatBookNotNull(existingBook);

        BookDto bookDto = teamBookMapper.toApi(existingBook);

        //so I actually need a view here, that pulls definitions, based on their presence in the team book references

        //I can actually pull the definitions here, need a different query.

        //then I can pull override definitions, I really oughta make a view.

        List<TagDefinitionDto> tagDefinitionDtos = new ArrayList<>();

        List<TeamDictionaryTagEntity> wordsInBook = teamDictionaryTagRepository.findDefinitionsByBookId(existingBook.getId());
        List<TeamBookOverrideEntity> overrides = teamBookOverrideRepository.findDefinitionsByBookId(existingBook.getId());

        for (TeamDictionaryTagEntity word : wordsInBook) {

            TagDefinitionDto bookTag = tagDefinitionMapper.toApi(word);
            tagDefinitionDtos.add(bookTag);
        }

        for (TeamBookOverrideEntity override : overrides) {

            TagDefinitionDto overrideTag = tagOverrideMapper.toApi(override);
            overrideTag.setCreatedDate(override.getOverrideDate());
            overrideTag.setOverride(true);

            tagDefinitionDtos.add(overrideTag);
        }

        bookDto.setDefinitions(tagDefinitionDtos);

        return bookDto;
    }


    public BookReferenceDto createCommunityDictionaryBook(UUID organizationId, UUID memberId, String bookName) {
        return null;
    }



    private TagDefinitionDto toDto(TeamDictionaryTagEntity dictionaryTag) {

        TagDefinitionDto tagDefinitionDto = new TagDefinitionDto();
        tagDefinitionDto.setTagName(dictionaryTag.getTagName());
        tagDefinitionDto.setDefinition(dictionaryTag.getDefinition());

        return tagDefinitionDto;
    }

    private TeamDictionaryTagEntity createNewTag(LocalDateTime now, TeamDto myTeam, TagDefinitionInputDto tagDefinitionInputDto) {

        //first check for existing tombstones by the same name, resurrect if needed

        resurrectTombstoneTagsWithSameName(now, myTeam, tagDefinitionInputDto);

        TeamDictionaryTagEntity newTag = new TeamDictionaryTagEntity();
        newTag.setId(UUID.randomUUID());
        newTag.setOrganizationId(myTeam.getOrganizationId());
        newTag.setTeamId(myTeam.getId());
        newTag.setCreationDate(now);
        newTag.setLastModifiedDate(now);
        newTag.setLowerCaseTagName(tagDefinitionInputDto.getTagName().toLowerCase());
        newTag.setTagName(tagDefinitionInputDto.getTagName());
        newTag.setDefinition(tagDefinitionInputDto.getDefinition());

        teamDictionaryTagRepository.save(newTag);

        return newTag;
    }

    private void resurrectTombstoneTagsWithSameName(LocalDateTime now, TeamDto myTeam, TagDefinitionInputDto tagDefinitionInputDto) {

        List<TeamDictionaryTombstoneEntity> matchingTombstones = teamDictionaryTombstoneRepository.findByTeamIdAndLowerCaseTagName(
                myTeam.getId(), tagDefinitionInputDto.getTagName().toLowerCase());

        for (TeamDictionaryTombstoneEntity tombstoneEntity : matchingTombstones) {
            tombstoneEntity.setReviveDate(now);
        }

        teamDictionaryTombstoneRepository.save(matchingTombstones);

    }

    private TeamDictionaryTagEntity refactorExistingTag(LocalDateTime now, TeamDictionaryTagEntity existingTag, TagDefinitionInputDto tagDefinitionInputDto) {

        if (hasTagCaseChange(existingTag, tagDefinitionInputDto)) {

            existingTag.setTagName(tagDefinitionInputDto.getTagName());
            existingTag.setDefinition(tagDefinitionInputDto.getDefinition());
            existingTag.setLastModifiedDate(now);

            teamDictionaryTagRepository.save(existingTag);
        } else if (hasDefinitionChangeOnly(existingTag, tagDefinitionInputDto)) {
            existingTag.setDefinition(tagDefinitionInputDto.getDefinition());
            existingTag.setLastModifiedDate(now);

            teamDictionaryTagRepository.save(existingTag);
        } else {

            //name change, create a tombstone link

            TeamDictionaryTombstoneEntity teamDictionaryTombstoneEntity = new TeamDictionaryTombstoneEntity();

            teamDictionaryTombstoneEntity.setId(UUID.randomUUID());
            teamDictionaryTombstoneEntity.setTeamId(existingTag.getTeamId());
            teamDictionaryTombstoneEntity.setOrganizationId(existingTag.getOrganizationId());
            teamDictionaryTombstoneEntity.setLowerCaseTagName(existingTag.getLowerCaseTagName());
            teamDictionaryTombstoneEntity.setDeadTagName(existingTag.getTagName());
            teamDictionaryTombstoneEntity.setDeadDefinition(existingTag.getDefinition());

            teamDictionaryTombstoneEntity.setRipDate(now);
            teamDictionaryTombstoneEntity.setForwardTo(existingTag.getId());

            teamDictionaryTombstoneRepository.save(teamDictionaryTombstoneEntity);

            //then edit the original

            existingTag.setTagName(tagDefinitionInputDto.getTagName());
            existingTag.setLowerCaseTagName(tagDefinitionInputDto.getTagName().toLowerCase());
            existingTag.setDefinition(tagDefinitionInputDto.getDefinition());
            existingTag.setLastModifiedDate(now);

            teamDictionaryTagRepository.save(existingTag);

        }

        return existingTag;
    }

    private boolean hasDefinitionChangeOnly(TeamDictionaryTagEntity existingTag, TagDefinitionInputDto tagDefinitionInputDto) {
        return existingTag.getTagName().equals(tagDefinitionInputDto.getTagName());
    }

    private boolean hasTagCaseChange(TeamDictionaryTagEntity existingTag, TagDefinitionInputDto tagDefinitionInputDto) {
        String existingTagKey = existingTag.getLowerCaseTagName();
        String newTagKey = tagDefinitionInputDto.getTagName().toLowerCase();

        return existingTagKey.equals(newTagKey) && !existingTag.getTagName().equals(tagDefinitionInputDto.getTagName());
    }

    public void touchBlankDefinition(UUID organizationId, UUID memberId, String tag) {
        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndLowerCaseTagName(myTeam.getId(), tag);

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
            TeamDictionaryTagEntity existingTag = teamDictionaryTagRepository.findByTeamIdAndLowerCaseTagName(myTeam.getId(), tag);

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

    public List<TagDefinitionDto> getUndefinedTeamDictionaryTerms(UUID organizationId, UUID memberId) {

        TeamDto myTeam = teamService.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        List<TeamDictionaryTagEntity> undefinedTerms = teamDictionaryTagRepository.findByTeamIdAndBlankDefinition(myTeam.getId());

        List<TagDefinitionDto> blankDefs = new ArrayList<>();

        for (TeamDictionaryTagEntity undefinedTerm : undefinedTerms) {
            TagDefinitionDto blankTag = new TagDefinitionDto(undefinedTerm.getTagName(), null,
                    undefinedTerm.getCreationDate(), undefinedTerm.getLastModifiedDate(), false);

            blankDefs.add(blankTag);
        }

        return blankDefs;
    }

    public List<TagDefinitionDto> getPendingTeamDictionaryTerms(UUID organizationId, UUID id) {
        return null;
    }

    public TagDefinitionDto acceptPendingTagIntoDictionary(UUID organizationId, UUID id, PendingTagReferenceDto pendingTagReferenceDto) {
        return null;

    }



    public void rejectPendingTag(UUID organizationId, UUID id, PendingTagReferenceDto pendingTagReferenceDto) {

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

    public TagDefinitionDto refactorTeamBookDefinition(UUID organizationId, UUID id, String bookName, String tagName, TagDefinitionInputDto tagDefinitionInputDto) {
        return null;
    }
}
