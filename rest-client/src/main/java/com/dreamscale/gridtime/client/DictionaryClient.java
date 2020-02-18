package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.dictionary.*;
import feign.Headers;
import feign.Param;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface DictionaryClient {

    //operates in global space across all scopes

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.TAG_PATH + "/{tagName}")
    TagDefinitionWithDetailsDto getDefinition(@Param("tagName") String tagName);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.TAG_PATH + "/{tagName}")
    TagDefinitionDto createOrRefactorDefinition(@Param("tagName") String tagName, TagDefinitionInputDto tagDefinitionInputDto);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PROMOTE_PATH)
    TagDefinitionDto promoteDefinition(@Param("tagName") String tagName);

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.UNDEFINED_PATH)
    List<TagDefinitionDto> getUndefinedTeamDictionaryTerms();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.PENDING_PATH)
    List<TagDefinitionDto> getPendingTeamDictionaryTerms();

    //team dictionary promotion

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH)
    List<TagDefinitionDto> getTeamDictionary();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH)
    List<TagDefinitionDto> getCommunityDictionary();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.PENDING_PATH)
    List<PendingTagReferenceDto> getPendingCommunityDefinitions();

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.ACCEPT_PATH)
    TagDefinitionDto acceptPendingTagIntoDictionary(PendingTagReferenceDto pendingTagReferenceDto);

    @RequestLine("POST "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.REJECT_PATH)
    void rejectPendingTag(PendingTagReferenceDto pendingTagReferenceDto);

    //organization dictionaries into books

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    DictionaryBookDto createTeamDictionaryBook(@Param("bookName") String bookName);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    DictionaryBookDto createCommunityDictionaryBook(@Param("bookName") String bookName);

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    List<TagDefinitionDto> getTeamBook(@Param("bookName") String bookName);

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    List<TagDefinitionDto> getCommunityBook(@Param("bookName") String bookName);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PULL_PATH)
    DictionaryBookDto pullDefinitionIntoTeamBook(@Param("bookName") String bookName, @Param("tagName") String tagName);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}" + ResourcePaths.PULL_PATH)
    DictionaryBookDto pullDefinitionIntoCommunityBook(@Param("bookName") String bookName, @Param("tagName") String tagName);

    //refactor definitions inside of books

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.TAG_PATH + "/{tagName}")
    TagDefinitionDto refactorTeamBookDefinition(@Param("bookName") String bookName, @Param("tagName") String tagName,
                                                TagDefinitionInputDto tagDefinitionInputDto);


}
