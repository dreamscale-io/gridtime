package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.dictionary.*;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.DictionaryService;
import com.dreamscale.gridtime.core.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.DICTIONARY_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class DictionaryResource {

    @Autowired
    DictionaryService dictionaryService;

    @Autowired
    OrganizationService organizationService;

    /**
     * Retrieves the definition for a specified dictionary word across all Scopes
     *
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.WORD_PATH + "/{wordName}" )
    public WordDefinitionWithDetailsDto getWord(@PathVariable("wordName") String wordName) {
        RequestContext context = RequestContext.get();
        log.info("getWord, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getWord(invokingMember.getOrganizationId(), invokingMember.getId(), wordName);
    }

    /**
     * Refactors the definition for a specified dictionary word, and sends notifications to subscribers.
     *
     * Always updates definition at team scope, without promoting the new version to community, and without updating any of the forks
     *
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WORD_PATH + "/{wordName}" )
    public WordDefinitionDto createOrRefactorWord(@PathVariable("wordName") String wordName, @RequestBody WordDefinitionInputDto wordDefinitionInputDto) {
        RequestContext context = RequestContext.get();
        log.info("createOrRefactorWord, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.createOrRefactorWord(invokingMember.getOrganizationId(), invokingMember.getId(), wordName, wordDefinitionInputDto);
    }

    /**
     * Promotes the existing team word definition to community scope, and sends a notification to community.
     *
     * These new definitions can be accepted or rejected by the community, and will be pending until approved.
     *
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PROMOTE_PATH)
    public WordDefinitionDto promoteWordToCommunityScope(@PathVariable("wordName") String wordName) {
        RequestContext context = RequestContext.get();
        log.info("promoteWordToCommunityScope, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.promoteWordToCommunityScope(invokingMember.getOrganizationId(), invokingMember.getId(), wordName);
    }

    /**
     * Retrieves all word definitions within the team's scope
     *
     * Intended to be locally cached, and used as a reference for autocomplete
     *
     * Dictionary updates will be pushed over talk
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH )
    public List<WordDefinitionDto> getGlobalTeamDictionary() {
        RequestContext context = RequestContext.get();
        log.info("getGlobalTeamDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getGlobalTeamDictionary(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the community scope dictionary
     *
     * Intended to be locally cached, and used as a reference for autocomplete
     *
     * Dictionary updates will be pushed over talk
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH )
    public List<WordDefinitionDto> getGlobalCommunityDictionary() {
        RequestContext context = RequestContext.get();
        log.info("getGlobalCommunityDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getGlobalCommunityDictionary(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Creates a new "Book", with an editable scope, for pulling in existing word definitions.
     *
     * Teams can organize their definitions into groups.  The same word definitions can exist in multiple books.
     *
     * New Book scopes allow edited of words without affecting the global definitions.
     *
     * Each new {bookName} must be unique.
     *
     * @return DictionaryBookDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public BookReferenceDto createTeamBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("createTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.createTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }

    /**
     * Updates an existing team book definition, with details such as changing the name
     *
     * Each new {bookName} must be unique.
     *
     * @return DictionaryBookDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PutMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public BookReferenceDto updateTeamBook(@PathVariable("bookName") String bookName, @RequestBody RefactorBookInputDto refactorBookInputDto) {
        RequestContext context = RequestContext.get();
        log.info("updateTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.updateTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, refactorBookInputDto);
    }

    /**
     * Creates a new "Book" editable scope for pulling in existing definitions.
     *
     * Communities can organize their definitions into groups.  The same tags can exist in multiple books.
     *
     * New Book scopes can be edited without affecting the primary community definitions.
     *
     * @return DictionaryBookDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public BookReferenceDto createCommunityBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("createCommunityBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.createCommunityBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }

    /**
     * Retrieves all the available book references within team scope
     *
     * @return List<BookReferenceDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH)
    public List<BookReferenceDto> getTeamBooks() {
        RequestContext context = RequestContext.get();
        log.info("getTeamBooks, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getAllTeamBooks(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves all the available book references within community scope
     *
     * @return List<BookReferenceDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH)
    public List<BookReferenceDto> getCommunityBooks() {
        RequestContext context = RequestContext.get();
        log.info("getCommunityBooks, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getAllCommunityBooks(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves all word definitions for the specified Book
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public BookDto getTeamBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("getTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }


    /**
     * Retrieves all the definitions for the specified Book
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public BookDto getCommunityBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("getCommunityBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getCommunityBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }


    /**
     * Refactors the specified definition for a word, within the scope of a book, but not for the global team scope
     *
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" )
    public WordDefinitionDto refactorWordInsideTeamBook(@PathVariable("bookName") String bookName, @PathVariable("wordName") String wordName, @RequestBody WordDefinitionInputDto wordDefinitionInputDto) {
        RequestContext context = RequestContext.get();
        log.info("refactorWordInsideTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.refactorWordInsideTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, wordName, wordDefinitionInputDto);
    }

    /**
     * Pulls the global team definition for a specified word into the specified Team Book
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PULL_PATH)
    public WordDefinitionDto pullWordIntoTeamBook(@PathVariable("bookName") String bookName, @PathVariable("wordName") String wordName) {
        RequestContext context = RequestContext.get();
        log.info("pullWordIntoTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.pullWordIntoTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, wordName);
    }

    /**
     * Retrieves the full history details definition of the specified word in the specified Team Book
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}")
    public WordDefinitionWithDetailsDto getTeamBookWord(@PathVariable("bookName") String bookName, @PathVariable("wordName") String wordName) {
        RequestContext context = RequestContext.get();
        log.info("getTeamBookWord, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getTeamBookWord(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, wordName);
    }


    /**
     * Pulls the global community definition for a specified word into the specified Community Book
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PULL_PATH)
    public WordDefinitionDto pullWordIntoCommunityBook(@PathVariable("bookName") String bookName, @PathVariable("wordName") String wordName) {
        RequestContext context = RequestContext.get();
        log.info("pullWordIntoCommunityBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.pullWordIntoCommunityBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, wordName);
    }

    /**
     * Retrieves the team scope dictionary where definitions are still missing, across all Books
     *
     * So you know which new words need to be filled in
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.UNDEFINED_PATH )
    public List<WordDefinitionDto> getUndefinedTeamWords() {
        RequestContext context = RequestContext.get();
        log.info("getUndefinedTeamWords, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getUndefinedTeamWords(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the team scope dictionary where definitions are promoted and still pending
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.PENDING_PATH )
    public List<WordDefinitionDto> getPromotionPendingTeamWords() {
        RequestContext context = RequestContext.get();
        log.info("getPromotionPendingTeamWords, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getPromotionPendingTeamWords(invokingMember.getOrganizationId(), invokingMember.getId());
    }


    /**
     * Retrieves the list of pending community definitions
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.PENDING_PATH)
    public List<PendingWordReferenceDto> getPendingCommunityWords() {
        RequestContext context = RequestContext.get();
        log.info("getPendingCommunityWords, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getPendingCommunityWords(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Accepts the specified definition, and merges it into the community library
     *
     * @return WordDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.ACCEPT_PATH)
    public WordDefinitionDto acceptPendingWordIntoCommunityScope(@RequestBody PendingWordReferenceDto pendingWordReferenceDto) {
        RequestContext context = RequestContext.get();
        log.info("acceptPendingWordIntoCommunityScope, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.acceptPendingWordIntoCommunityScope(invokingMember.getOrganizationId(), invokingMember.getId(), pendingWordReferenceDto);
    }

    /**
     * Retrieves the list of pending community definitions
     *
     * @return List<WordDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.REJECT_PATH)
    public void rejectPendingCommunityWord(@RequestBody PendingWordReferenceDto pendingWordReferenceDto) {
        RequestContext context = RequestContext.get();
        log.info("rejectPendingCommunityWord, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        dictionaryService.rejectPendingCommunityWord(invokingMember.getOrganizationId(), invokingMember.getId(), pendingWordReferenceDto);
    }
}
