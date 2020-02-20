package com.dreamscale.gridtime.core.capability.directory;

import com.dreamscale.gridtime.api.dictionary.*;
import com.dreamscale.gridtime.api.team.TeamDto;
import com.dreamscale.gridtime.core.domain.dictionary.*;
import com.dreamscale.gridtime.core.exception.ValidationErrorCodes;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import com.dreamscale.gridtime.core.service.TimeService;
import lombok.extern.slf4j.Slf4j;
import org.dreamscale.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DictionaryCapability {


    @Autowired
    TimeService timeService;

    @Autowired
    TeamMembershipCapability teamMembership;

    @Autowired
    TeamDictionaryWordRepository teamDictionaryWordRepository;

    @Autowired
    TeamDictionaryWordTombstoneRepository teamDictionaryWordTombstoneRepository;

    @Autowired
    TeamBookRepository teamBookRepository;

    @Autowired
    TeamBookWordRepository teamBookWordRepository;

    @Autowired
    TeamBookWordOverrideRepository teamBookWordOverrideRepository;

    @Autowired
    TeamBookWordTombstoneRepository teamBookWordTombstoneRepository;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<WordDefinitionWithDetailsDto, TeamDictionaryWordEntity> wordDefinitionWithDetailsMapper;
    private DtoEntityMapper<WordDefinitionWithDetailsDto, TeamBookWordOverrideEntity> wordOverrideWithDetailsMapper;

    private DtoEntityMapper<BookReferenceDto, TeamBookEntity> teamBookReferenceMapper;
    private DtoEntityMapper<BookDto, TeamBookEntity> teamBookMapper;
    private DtoEntityMapper<WordDefinitionDto, TeamDictionaryWordEntity> wordDefinitionMapper;
    private DtoEntityMapper<WordDefinitionDto, TeamBookWordOverrideEntity> wordDefinitionOverrideMapper;
    private DtoEntityMapper<WordTombstoneDto, TeamDictionaryWordTombstoneEntity> wordTombstoneMapper;
    private DtoEntityMapper<WordTombstoneDto, TeamBookWordTombstoneEntity> bookWordTombstoneMapper;


    @PostConstruct
    private void init() {
        wordDefinitionMapper = mapperFactory.createDtoEntityMapper(WordDefinitionDto.class, TeamDictionaryWordEntity.class);
        wordDefinitionOverrideMapper = mapperFactory.createDtoEntityMapper(WordDefinitionDto.class, TeamBookWordOverrideEntity.class);

        wordDefinitionWithDetailsMapper = mapperFactory.createDtoEntityMapper(WordDefinitionWithDetailsDto.class, TeamDictionaryWordEntity.class);
        wordOverrideWithDetailsMapper = mapperFactory.createDtoEntityMapper(WordDefinitionWithDetailsDto.class, TeamBookWordOverrideEntity.class);

        teamBookReferenceMapper = mapperFactory.createDtoEntityMapper(BookReferenceDto.class, TeamBookEntity.class );
        teamBookMapper = mapperFactory.createDtoEntityMapper(BookDto.class, TeamBookEntity.class);

        wordTombstoneMapper = mapperFactory.createDtoEntityMapper(WordTombstoneDto.class, TeamDictionaryWordTombstoneEntity.class);
        bookWordTombstoneMapper = mapperFactory.createDtoEntityMapper(WordTombstoneDto.class, TeamBookWordTombstoneEntity.class);

    }


    public WordDefinitionWithDetailsDto getWord(UUID organizationId, UUID memberId, String wordName) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        LocalDateTime now = timeService.now();

        //get all definitions, across all scopes, and then all tombstone references that are forwarded here

        TeamDictionaryWordEntity existingWord = findByTeamAndCaseInsensitiveWord(myTeam.getId(), wordName);

        WordDefinitionWithDetailsDto wordDefinition = wordDefinitionWithDetailsMapper.toApi(existingWord);

        if (existingWord != null) {

            List<TeamDictionaryWordTombstoneEntity> tombstones = teamDictionaryWordTombstoneRepository.findByForwardToOrderByRipDate(existingWord.getId());

            List<WordTombstoneDto> tombstoneDtos = new ArrayList<>();

            for (TeamDictionaryWordTombstoneEntity tombstone: tombstones) {
                WordTombstoneDto tombstoneDto = new WordTombstoneDto();
                tombstoneDto.setDeadWordName(tombstone.getDeadWordName());
                tombstoneDto.setDeadDefinition(tombstone.getDeadDefinition());
                tombstoneDto.setRipDate(tombstone.getRipDate());
                tombstone.setRipByMemberId(tombstone.getRipByMemberId());
                tombstoneDto.setReviveDate(tombstone.getReviveDate());

                tombstoneDtos.add(tombstoneDto);
            }

            wordDefinition.setTombstones(tombstoneDtos);
        }

        return wordDefinition;
    }

    public WordDefinitionWithDetailsDto getTeamBookWord(UUID organizationId, UUID memberId, String bookName, String wordName) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);
        validateWordNotNull(wordName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        validatBookNotNull(existingBook);

        TeamBookWordOverrideEntity override = teamBookWordOverrideRepository.findWordOverrideByBookIdAndLowerCaseWordName(existingBook.getId(), wordName);

        WordDefinitionWithDetailsDto bookWord = null;

        if (override == null) {
            //use the global definition of the word

            TeamDictionaryWordEntity globalWord = teamDictionaryWordRepository.findByTeamIdAndLowerCaseWordName(myTeam.getId(), wordName.toLowerCase());
            bookWord = wordDefinitionWithDetailsMapper.toApi(globalWord);
        } else {

            bookWord = wordOverrideWithDetailsMapper.toApi(override);
            bookWord.setCreationDate(override.getOverrideDate());

            List<TeamBookWordTombstoneEntity> tombstoneEntities = teamBookWordTombstoneRepository.findByTeamBookWordIdOrderByRipDate(override.getTeamBookWordId());

            List<WordTombstoneDto> tombstoneDtos = bookWordTombstoneMapper.toApiList(tombstoneEntities);

            bookWord.setTombstones(tombstoneDtos);

        }

        return bookWord;
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

    private void validateWordNotNull(String wordName) {
        if (wordName == null || wordName.length() == 0) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_WORD, "Word name cant be blank.");
        }
    }

    private void validateWordNotNull(TeamDictionaryWordEntity wordEntity) {
        if (wordEntity == null ) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_WORD, "Word could not be found.");
        }
    }

    private void validatBookNotNull(TeamBookEntity book) {
        if (book == null ) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_BOOK, "Book not found");
        }
    }

    private void validateBookWordNotNull(TeamBookWordEntity bookWord) {
        if (bookWord == null ) {
            throw new BadRequestException(ValidationErrorCodes.WORD_NOT_FOUND_IN_BOOK, "Word not found in Book");
        }
    }


    private TeamDictionaryWordEntity findByTeamAndCaseInsensitiveWord(UUID teamId, String wordName) {

        String lowerCaseWord = wordName.toLowerCase();

        return teamDictionaryWordRepository.findByTeamIdAndLowerCaseWordName(teamId, lowerCaseWord);
    }


    public WordDefinitionDto createOrRefactorWord(UUID organizationId, UUID memberId, String originalWordName, WordDefinitionInputDto wordDefinitionInputDto) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateWordNotNull(originalWordName);
        validateWordNotNull(wordDefinitionInputDto.getWordName());

        TeamDictionaryWordEntity existingWord = findByTeamAndCaseInsensitiveWord(myTeam.getId(), originalWordName);

        LocalDateTime now = timeService.now();

        TeamDictionaryWordEntity updatedWord = null;
        if (existingWord != null) {
            updatedWord = refactorExistingTeamWord(now, memberId, existingWord, wordDefinitionInputDto);
        } else {
            updatedWord = createNewTeamWord(now, myTeam, memberId, wordDefinitionInputDto);
        }

        return toDto(updatedWord);
    }

    public BookReferenceDto createTeamBook(UUID organizationId, UUID memberId, String bookName) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        if (existingBook == null) {
            //create a new book

            TeamBookEntity bookEntity = new TeamBookEntity();
            bookEntity.setId(UUID.randomUUID());
            bookEntity.setBookName(bookName);
            bookEntity.setLowerCaseBookName(bookName.toLowerCase());
            bookEntity.setOrganizationId(organizationId);
            bookEntity.setCreatedByMemberId(memberId);
            bookEntity.setTeamId(myTeam.getId());
            bookEntity.setCreationDate(now);
            bookEntity.setLastModifiedDate(now);
            bookEntity.setBookStatus(BookStatus.ACTIVE);

            teamBookRepository.save(bookEntity);

            existingBook = bookEntity;

        }

        return teamBookReferenceMapper.toApi(existingBook);
    }

    public BookReferenceDto updateTeamBook(UUID organizationId, UUID memberId, String originalBookName, RefactorBookInputDto refactorBookInputDto) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(originalBookName);
        validateBookNotNull(refactorBookInputDto.getNewBookName());

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), originalBookName.toLowerCase());

        validateBookNotNull(existingBook);

        LocalDateTime now = timeService.now();

        existingBook.setBookName(refactorBookInputDto.getNewBookName());
        existingBook.setLowerCaseBookName(refactorBookInputDto.getNewBookName().toLowerCase());
        existingBook.setLastModifiedDate(now);

        teamBookRepository.save(existingBook);

        return teamBookReferenceMapper.toApi(existingBook);
    }

    public BookReferenceDto archiveTeamBook(UUID organizationId, UUID memberId, String bookName) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        validateBookNotNull(existingBook);

        LocalDateTime now = timeService.now();

        existingBook.setBookStatus(BookStatus.ARCHIVED);
        existingBook.setLastModifiedDate(now);

        teamBookRepository.save(existingBook);

        return teamBookReferenceMapper.toApi(existingBook);
    }

    public WordDefinitionDto pullWordIntoTeamBook(UUID organizationId, UUID memberId, String bookName, String wordName) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);
        validateWordNotNull(wordName);

        TeamDictionaryWordEntity word = findByTeamAndCaseInsensitiveWord(myTeam.getId(), wordName);

        validateWordNotNull(word);

        TeamBookWordEntity existingWord = pullWordInsideBook(myTeam.getId(), memberId, bookName, word.getId());

        return wordDefinitionMapper.toApi(word);
    }

    @Transactional
    public void deleteWordFromTeamBook(UUID organizationId, UUID memberId, String bookName, String wordName) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);
        validateWordNotNull(wordName);

        TeamBookEntity teamBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        validateBookNotNull(teamBook);

        TeamBookWordOverrideEntity override = teamBookWordOverrideRepository.findWordOverrideByBookIdAndLowerCaseWordName(teamBook.getId(), wordName.toLowerCase());

        if (override == null) {
            //this is the first modification, delete the link

            TeamDictionaryWordEntity globalWord = findByTeamAndCaseInsensitiveWord(myTeam.getId(), wordName);
            validateWordNotNull(globalWord);

            TeamBookWordEntity bookWord = teamBookWordRepository.findByTeamBookIdAndTeamWordId(teamBook.getId(), globalWord.getId());

            teamBookWordRepository.delete(bookWord.getId());
        } else {
            //this is already an override, delete all the tombstones, and the override, and the link

            List<TeamBookWordTombstoneEntity> tombstones = teamBookWordTombstoneRepository.findByTeamBookWordIdOrderByRipDate(override.getTeamBookWordId());

            teamBookWordTombstoneRepository.delete(tombstones);

            teamBookWordOverrideRepository.delete(override);

            teamBookWordRepository.delete(override.getTeamBookWordId());
        }

    }

    @Transactional
    public WordDefinitionDto refactorWordInsideTeamBook(UUID organizationId, UUID memberId, String bookName, String wordName, WordDefinitionInputDto wordDefinitionInputDto) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);
        validateWordNotNull(wordName);
        validateWordNotNull(wordDefinitionInputDto.getWordName());

        TeamBookEntity teamBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        validateBookNotNull(teamBook);

        TeamBookWordOverrideEntity override = teamBookWordOverrideRepository.findWordOverrideByBookIdAndLowerCaseWordName(teamBook.getId(), wordName.toLowerCase());

        LocalDateTime now = timeService.now();
        Long nanoTime = timeService.nanoTime();

        if (override == null) {
            //this is the first modification, create the first override

            TeamDictionaryWordEntity globalWord = findByTeamAndCaseInsensitiveWord(myTeam.getId(), wordName);
            validateWordNotNull(globalWord);

            TeamBookWordEntity bookWord = teamBookWordRepository.findByTeamBookIdAndTeamWordId(teamBook.getId(), globalWord.getId());

            validateBookWordNotNull(bookWord);

            override = new TeamBookWordOverrideEntity();
            override.setTeamBookId(teamBook.getId());
            override.setTeamBookWordId(bookWord.getId());
            override.setCreatedByMemberId(memberId);
            override.setLastModifiedByMemberId(memberId);
            override.setOverrideDate(now);
            override.setLastModifiedDate(now);
            override.setWordName(wordDefinitionInputDto.getWordName());
            override.setLowerCaseWordName(wordDefinitionInputDto.getWordName().toLowerCase());
            override.setDefinition(wordDefinitionInputDto.getDefinition());

            teamBookWordOverrideRepository.save(override);

            bookWord.setModifiedStatus(WordModifiedStatus.MODIFIED);

            teamBookWordRepository.save(bookWord);

            TeamBookWordTombstoneEntity tombstone = new TeamBookWordTombstoneEntity();
            tombstone.setId(UUID.randomUUID());
            tombstone.setTeamBookWordId(bookWord.getId());
            tombstone.setDeadWordName(globalWord.getWordName());
            tombstone.setLowerCaseWordName(globalWord.getWordName().toLowerCase());
            tombstone.setDeadDefinition(globalWord.getDefinition());
            tombstone.setRipDate(now);
            tombstone.setRipByMemberId(memberId);

            teamBookWordTombstoneRepository.save(tombstone);


        } else {
            //existing override, so tombstone the last definition, then change

            TeamBookWordTombstoneEntity tombstone = new TeamBookWordTombstoneEntity();
            tombstone.setId(UUID.randomUUID());
            tombstone.setTeamBookWordId(override.getTeamBookWordId());
            tombstone.setDeadWordName(override.getWordName());
            tombstone.setLowerCaseWordName(override.getWordName().toLowerCase());
            tombstone.setDeadDefinition(override.getDefinition());
            tombstone.setRipDate(now);
            tombstone.setRipByMemberId(memberId);

            teamBookWordTombstoneRepository.save(tombstone);

            override.setWordName(wordDefinitionInputDto.getWordName());
            override.setLowerCaseWordName(wordDefinitionInputDto.getWordName().toLowerCase());
            override.setDefinition(wordDefinitionInputDto.getDefinition());
            override.setLastModifiedDate(now);
            override.setLastModifiedByMemberId(memberId);

            teamBookWordOverrideRepository.save(override);
        }

        return toDto(override);
    }

    private TeamBookWordEntity pullWordInsideBook(UUID teamId, UUID memberId, String bookName, UUID wordId) {

        TeamBookEntity book = teamBookRepository.findByTeamIdAndLowerCaseBookName(teamId, bookName.toLowerCase());

        validateBookNotNull(book);

        TeamBookWordEntity wordInBook = teamBookWordRepository.findByTeamBookIdAndTeamWordId(book.getId(), wordId);

        if (wordInBook == null) {
            wordInBook = new TeamBookWordEntity();
            wordInBook.setId(UUID.randomUUID());
            wordInBook.setTeamBookId(book.getId());
            wordInBook.setTeamWordId(wordId);
            wordInBook.setModifiedStatus(WordModifiedStatus.UNCHANGED);
            wordInBook.setPullDate(timeService.now());
            wordInBook.setPulledByMemberId(memberId);

            teamBookWordRepository.save(wordInBook);
        }

        return wordInBook;
    }

    public BookDto getTeamBook(UUID organizationId, UUID memberId, String bookName) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateBookNotNull(bookName);

        TeamBookEntity existingBook = teamBookRepository.findByTeamIdAndLowerCaseBookName(myTeam.getId(), bookName.toLowerCase());

        validatBookNotNull(existingBook);

        BookDto bookDto = teamBookMapper.toApi(existingBook);

        //so I actually need a view here, that pulls definitions, based on their presence in the team book references

        //I can actually pull the definitions here, need a different query.

        //then I can pull override definitions, I really oughta make a view.

        List<WordDefinitionDto> wordDefinitionDtos = new ArrayList<>();

        List<TeamDictionaryWordEntity> wordsInBook = teamDictionaryWordRepository.findWordsByBookId(existingBook.getId());
        List<TeamBookWordOverrideEntity> overrides = teamBookWordOverrideRepository.findWordOverridesByBookId(existingBook.getId());

        for (TeamDictionaryWordEntity word : wordsInBook) {

            WordDefinitionDto wordInBook = wordDefinitionMapper.toApi(word);
            wordDefinitionDtos.add(wordInBook);
        }

        for (TeamBookWordOverrideEntity override : overrides) {

            WordDefinitionDto overrideWord = wordDefinitionOverrideMapper.toApi(override);
            overrideWord.setCreatedDate(override.getOverrideDate());
            overrideWord.setOverride(true);

            wordDefinitionDtos.add(overrideWord);
        }

        bookDto.setDefinitions(wordDefinitionDtos);

        return bookDto;
    }

    public List<BookReferenceDto> getAllTeamBooks(UUID organizationId, UUID memberId) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        List<TeamBookEntity> teamBooks = teamBookRepository.findByTeamId(myTeam.getId());

        return teamBookReferenceMapper.toApiList(teamBooks);
    }


    public BookReferenceDto createCommunityBook(UUID organizationId, UUID memberId, String bookName) {
        return null;
    }



    private WordDefinitionDto toDto(TeamDictionaryWordEntity dictionaryWord) {

        return wordDefinitionMapper.toApi(dictionaryWord);

    }

    private WordDefinitionDto toDto(TeamBookWordOverrideEntity overrideWord) {

        WordDefinitionDto dto = wordDefinitionOverrideMapper.toApi(overrideWord);
        dto.setCreatedDate(overrideWord.getOverrideDate());
        dto.setOverride(true);

        WordDefinitionDto wordDefinitionDto = new WordDefinitionDto();
        wordDefinitionDto.setWordName(overrideWord.getWordName());
        wordDefinitionDto.setDefinition(overrideWord.getDefinition());

        return wordDefinitionDto;
    }


    private TeamDictionaryWordEntity createNewTeamWord(LocalDateTime now, TeamDto myTeam, UUID memberId, WordDefinitionInputDto wordDefinitionInputDto) {

        //first check for existing tombstones by the same name, resurrect if needed

        resurrectTeamTombstoneWordsWithSameName(now, myTeam, wordDefinitionInputDto);

        TeamDictionaryWordEntity newWord = new TeamDictionaryWordEntity();
        newWord.setId(UUID.randomUUID());
        newWord.setOrganizationId(myTeam.getOrganizationId());
        newWord.setTeamId(myTeam.getId());
        newWord.setCreatedByMemberId(memberId);
        newWord.setLastModifiedByMemberId(memberId);
        newWord.setCreationDate(now);
        newWord.setLastModifiedDate(now);
        newWord.setLowerCaseWordName(wordDefinitionInputDto.getWordName().toLowerCase());
        newWord.setWordName(wordDefinitionInputDto.getWordName());
        newWord.setDefinition(wordDefinitionInputDto.getDefinition());

        teamDictionaryWordRepository.save(newWord);

        return newWord;
    }

    private void resurrectTeamTombstoneWordsWithSameName(LocalDateTime now, TeamDto myTeam, WordDefinitionInputDto wordDefinitionInputDto) {

        List<TeamDictionaryWordTombstoneEntity> matchingTombstones = teamDictionaryWordTombstoneRepository.findByTeamIdAndLowerCaseWordName(
                myTeam.getId(), wordDefinitionInputDto.getWordName().toLowerCase());

        for (TeamDictionaryWordTombstoneEntity tombstoneEntity : matchingTombstones) {
            tombstoneEntity.setReviveDate(now);
        }

        teamDictionaryWordTombstoneRepository.save(matchingTombstones);

    }

    private TeamDictionaryWordEntity refactorExistingTeamWord(LocalDateTime now, UUID memberId, TeamDictionaryWordEntity existingWord, WordDefinitionInputDto wordDefinitionInputDto) {

        if (hasWordCaseChange(existingWord, wordDefinitionInputDto)) {

            existingWord.setWordName(wordDefinitionInputDto.getWordName());
            existingWord.setDefinition(wordDefinitionInputDto.getDefinition());
            existingWord.setLastModifiedDate(now);
            existingWord.setLastModifiedByMemberId(memberId);

            teamDictionaryWordRepository.save(existingWord);
        } else if (hasDefinitionChangeOnly(existingWord, wordDefinitionInputDto)) {
            existingWord.setDefinition(wordDefinitionInputDto.getDefinition());
            existingWord.setLastModifiedDate(now);
            existingWord.setLastModifiedByMemberId(memberId);

            teamDictionaryWordRepository.save(existingWord);
        } else {

            //name change, create a tombstone link

            TeamDictionaryWordTombstoneEntity teamDictionaryWordTombstoneEntity = new TeamDictionaryWordTombstoneEntity();

            teamDictionaryWordTombstoneEntity.setId(UUID.randomUUID());
            teamDictionaryWordTombstoneEntity.setTeamId(existingWord.getTeamId());
            teamDictionaryWordTombstoneEntity.setOrganizationId(existingWord.getOrganizationId());
            teamDictionaryWordTombstoneEntity.setLowerCaseWordName(existingWord.getLowerCaseWordName());
            teamDictionaryWordTombstoneEntity.setDeadWordName(existingWord.getWordName());
            teamDictionaryWordTombstoneEntity.setDeadDefinition(existingWord.getDefinition());
            teamDictionaryWordTombstoneEntity.setRipByMemberId(memberId);

            teamDictionaryWordTombstoneEntity.setRipDate(now);
            teamDictionaryWordTombstoneEntity.setForwardTo(existingWord.getId());

            teamDictionaryWordTombstoneRepository.save(teamDictionaryWordTombstoneEntity);

            //then edit the original

            existingWord.setWordName(wordDefinitionInputDto.getWordName());
            existingWord.setLowerCaseWordName(wordDefinitionInputDto.getWordName().toLowerCase());
            existingWord.setDefinition(wordDefinitionInputDto.getDefinition());
            existingWord.setLastModifiedDate(now);
            existingWord.setLastModifiedByMemberId(memberId);

            teamDictionaryWordRepository.save(existingWord);

        }

        return existingWord;
    }



    private boolean hasDefinitionChangeOnly(TeamDictionaryWordEntity existingWod, WordDefinitionInputDto wordDefinitionInputDto) {
        return existingWod.getWordName().equals(wordDefinitionInputDto.getWordName());
    }

    private boolean hasWordCaseChange(TeamDictionaryWordEntity existingWord, WordDefinitionInputDto wordDefinitionInputDto) {
        String existingWordKey = existingWord.getLowerCaseWordName();
        String newWordKey = wordDefinitionInputDto.getWordName().toLowerCase();

        return existingWordKey.equals(newWordKey) && !existingWord.getWordName().equals(wordDefinitionInputDto.getWordName());
    }

    public void touchBlankDefinition(UUID organizationId, UUID memberId, String wordName) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);
        validateWordNotNull(wordName);

        TeamDictionaryWordEntity existingWord = teamDictionaryWordRepository.findByTeamIdAndLowerCaseWordName(myTeam.getId(), wordName);

        if (existingWord == null) {
            LocalDateTime now = timeService.now();

            TeamDictionaryWordEntity newWord = new TeamDictionaryWordEntity();
            newWord.setOrganizationId(organizationId);
            newWord.setTeamId(myTeam.getId());
            newWord.setCreatedByMemberId(memberId);
            newWord.setLastModifiedByMemberId(memberId);
            newWord.setCreationDate(now);
            newWord.setLastModifiedDate(now);
            newWord.setWordName(wordName);
            newWord.setLowerCaseWordName(wordName.toLowerCase());
            teamDictionaryWordRepository.save(newWord);
        }
    }

    public void touchBlankDefinitions(UUID organizationId, UUID memberId, List<String> words) {
        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        LocalDateTime now = timeService.now();

        List<TeamDictionaryWordEntity> newWordEntries = new ArrayList<>();

        for (String word : words) {
            TeamDictionaryWordEntity existingWord = teamDictionaryWordRepository.findByTeamIdAndLowerCaseWordName(myTeam.getId(), word);

            if (existingWord == null) {
                TeamDictionaryWordEntity newWord = new TeamDictionaryWordEntity();
                newWord.setId(UUID.randomUUID());
                newWord.setOrganizationId(organizationId);
                newWord.setTeamId(myTeam.getId());
                newWord.setCreatedByMemberId(memberId);
                newWord.setLastModifiedByMemberId(memberId);
                newWord.setCreationDate(now);
                newWord.setLastModifiedDate(now);
                newWord.setWordName(word);
                newWord.setLowerCaseWordName(word.toLowerCase());
                newWordEntries.add(newWord);
            }
        }

        if (newWordEntries.size() > 0) {
            teamDictionaryWordRepository.save(newWordEntries);
        }
    }

    private void validateTeamExists(TeamDto myTeam) {
        if (myTeam == null) {
            throw new BadRequestException(ValidationErrorCodes.MISSING_OR_INVALID_TEAM, "Unable to find team for member.");
        }
    }


    public WordDefinitionDto promoteWordToCommunityScope(UUID organizationId, UUID id, String tagName) {
        return null;
    }


    public List<WordDefinitionDto> getGlobalTeamDictionary(UUID organizationId, UUID memberId) {
        return null;
    }

    public List<WordDefinitionDto> getGlobalCommunityDictionary(UUID organizationId, UUID memberId) {
        return null;
    }

    public List<PendingWordReferenceDto> getPendingCommunityWords(UUID organizationId, UUID id) {
        return null;
    }

    public List<WordDefinitionDto> getUndefinedTeamWords(UUID organizationId, UUID memberId) {

        TeamDto myTeam = teamMembership.getMyPrimaryTeam(organizationId, memberId);

        validateTeamExists(myTeam);

        List<TeamDictionaryWordEntity> undefinedWords = teamDictionaryWordRepository.findByTeamIdAndBlankDefinition(myTeam.getId());

        List<WordDefinitionDto> blankDefs = new ArrayList<>();

        for (TeamDictionaryWordEntity undefinedWord : undefinedWords) {
            WordDefinitionDto blankDefinition = new WordDefinitionDto(undefinedWord.getWordName(), null,
                    undefinedWord.getCreationDate(), undefinedWord.getLastModifiedDate(), false);

            blankDefs.add(blankDefinition);
        }

        return blankDefs;
    }

    public List<WordDefinitionDto> getPromotionPendingTeamWords(UUID organizationId, UUID id) {
        return null;
    }

    public WordDefinitionDto acceptPendingWordIntoCommunityScope(UUID organizationId, UUID id, PendingWordReferenceDto pendingWordReferenceDto) {
        return null;

    }



    public void rejectPendingCommunityWord(UUID organizationId, UUID id, PendingWordReferenceDto pendingWordReferenceDto) {

    }



    public BookDto getCommunityBook(UUID organizationId, UUID id, String bookName) {
        return null;
    }

    public WordDefinitionDto getDefinitionWithinTeamBook(UUID organizationId, UUID id, String bookName, String wordName) {
        return null;
    }

    public WordDefinitionDto pullWordIntoCommunityBook(UUID organizationId, UUID id, String bookName, String wordName) {
        return null;
    }



    public List<BookReferenceDto> getAllCommunityBooks(UUID organizationId, UUID memberId) {
        return null;
    }


}
