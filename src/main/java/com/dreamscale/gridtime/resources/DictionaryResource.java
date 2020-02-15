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
     * Retrieves the definition for a specified tag across all Scopes
     *
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.TAG_PATH + "/{tagName}" )
    public TagDefinitionWithDetailsDto getDefinition(@PathVariable("tagName") String tagName) {
        RequestContext context = RequestContext.get();
        log.info("getDefinition, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getDefinition(invokingMember.getOrganizationId(), invokingMember.getId(), tagName);
    }

    /**
     * Refactors the definition for a specified tag, and sends notifications to subscribers.
     *
     * Always updates definition at team scope, without promoting the new version to community, and without updating any of the forks
     *
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TAG_PATH + "/{tagName}" )
    public TagDefinitionDto refactorDefinition(@PathVariable("tagName") String tagName, @RequestBody TagRefactorInputDto tagRefactorInputDto) {
        RequestContext context = RequestContext.get();
        log.info("refactorDefinition, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.refactorDefinition(invokingMember.getOrganizationId(), invokingMember.getId(), tagName, tagRefactorInputDto);
    }

    /**
     * Promotes the existing team definition to community scope, and sends a notification to community.
     *
     * These new definitions can be accepted or rejected by the community, and will be pending until approved..
     *
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PROMOTE_PATH)
    public TagDefinitionDto promoteDefinition(@PathVariable("tagName") String tagName) {
        RequestContext context = RequestContext.get();
        log.info("promoteDefinition, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.promoteDefinition(invokingMember.getOrganizationId(), invokingMember.getId(), tagName);
    }

    /**
     * Retrieves the team scope dictionary
     *
     * Intended to be locally cached, and used as a reference for autocomplete
     *
     * Dictionary updates will be pushed over talk
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH )
    public List<TagDefinitionDto> getTeamDictionary() {
        RequestContext context = RequestContext.get();
        log.info("getTeamDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getTeamDictionary(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Creates a new "Book" editable scope for pulling in existing definitions.
     *
     * Teams can organize their definitions into groups.  The same tags can exist in multiple books.
     *
     * New Book scopes can be edited without affecting the primary definitions.
     *
     * @return DictionaryBookDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public DictionaryBookDto createTeamDictionaryBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("createTeamDictionaryBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.createTeamDictionaryBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
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
    public DictionaryBookDto createCommunityDictionaryBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("createCommunityDictionaryBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.createCommunityDictionaryBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }

    /**
     * Retrieves all the definitions for the specified Book
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public List<TagDefinitionDto> getTeamBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("getTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }

    /**
     * Retrieves all the definitions for the specified Book
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    public List<TagDefinitionDto> getCommunityBook(@PathVariable("bookName") String bookName) {
        RequestContext context = RequestContext.get();
        log.info("getCommunityBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getCommunityBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName);
    }

    /**
     * Retrieves a single definition for a specified tag within a book
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" )
    public TagDefinitionDto getDefinitionWithinTeamBook(@PathVariable("bookName") String bookName, @PathVariable("tagName") String tagName) {
        RequestContext context = RequestContext.get();
        log.info("getBookDefinition, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getDefinitionWithinTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, tagName);
    }

    /**
     * Refactors the specified definition for a tag, within the scope of a book, but not for the global team
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" )
    public TagDefinitionDto refactorTeamBookDefinition(@PathVariable("bookName") String bookName, @PathVariable("tagName") String tagName, @RequestBody TagRefactorInputDto tagRefactorInputDto) {
        RequestContext context = RequestContext.get();
        log.info("refactorTeamBookDefinition, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.refactorTeamBookDefinition(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, tagName, tagRefactorInputDto);
    }

    /**
     * Pulls the global team definition for a specified tag into the specified book
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PULL_PATH)
    public TagDefinitionDto pullDefinitionIntoTeamBook(@PathVariable("bookName") String bookName, @PathVariable("tagName") String tagName) {
        RequestContext context = RequestContext.get();
        log.info("pullDefinitionIntoTeamBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.pullDefinitionIntoTeamBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, tagName);
    }

    /**
     * Pulls the global team definition for a specified tag into the specified book
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PULL_PATH)
    public TagDefinitionDto pullDefinitionIntoCommunityBook(@PathVariable("bookName") String bookName, @PathVariable("tagName") String tagName) {
        RequestContext context = RequestContext.get();
        log.info("pullDefinitionIntoCommunityBook, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.pullDefinitionIntoCommunityBook(invokingMember.getOrganizationId(), invokingMember.getId(), bookName, tagName);
    }

    /**
     * Retrieves the team scope dictionary where definitions are still missing, across all Books
     *
     * So you know which new words need to be filled in
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.UNDEFINED_PATH )
    public List<TagDefinitionDto> getUndefinedTeamDictionaryTerms() {
        RequestContext context = RequestContext.get();
        log.info("getUndefinedTeamDictionaryTerms, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getUndefinedTeamDictionaryTerms(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the team scope dictionary where definitions are promoted and still pending
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.PENDING_PATH )
    public List<TagDefinitionDto> getPendingTeamDictionaryTerms() {
        RequestContext context = RequestContext.get();
        log.info("getPendingTeamDictionaryTerms, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getPendingTeamDictionaryTerms(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the community scope dictionary
     *
     * Intended to be locally cached, and used as a reference for autocomplete
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH )
    public List<TagDefinitionDto> getCommunityDictionary() {
        RequestContext context = RequestContext.get();
        log.info("getCommunityDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getCommunityDictionary(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Retrieves the list of pending community definitions
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.PENDING_PATH)
    public List<PendingTagReferenceDto> getPendingCommunityDefinitions() {
        RequestContext context = RequestContext.get();
        log.info("getPendingCommunityDefinitions, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.getPendingCommunityDefinitions(invokingMember.getOrganizationId(), invokingMember.getId());
    }

    /**
     * Accepts the specified definition, and merges it into the community library
     *
     * @return TagDefinitionDto
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.ACCEPT_PATH)
    public TagDefinitionDto acceptPendingTagIntoDictionary(@RequestBody PendingTagReferenceDto pendingTagReferenceDto) {
        RequestContext context = RequestContext.get();
        log.info("acceptPendingTagIntoDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        return dictionaryService.acceptPendingTagIntoDictionary(invokingMember.getOrganizationId(), invokingMember.getId(), pendingTagReferenceDto);
    }

    /**
     * Retrieves the list of pending community definitions
     *
     * @return List<TagDefinitionDto>
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping(ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.REJECT_PATH)
    public void rejectPendingTag(@RequestBody PendingTagReferenceDto pendingTagReferenceDto) {
        RequestContext context = RequestContext.get();
        log.info("acceptPendingTagIntoDictionary, user={}", context.getRootAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getRootAccountId());
        dictionaryService.rejectPendingTag(invokingMember.getOrganizationId(), invokingMember.getId(), pendingTagReferenceDto);
    }
}
