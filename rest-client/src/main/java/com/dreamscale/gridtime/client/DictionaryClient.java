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

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.WORD_PATH + "/{wordName}")
    WordDefinitionWithDetailsDto getWord(@Param("wordName") String wordName);


    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.WORD_PATH + "/{wordName}")
    WordDefinitionDto createOrRefactorWord(@Param("wordName") String wordName, WordDefinitionInputDto wordDefinitionInputDto);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PROMOTE_PATH)
    WordDefinitionDto promoteWordToCommunityScope(@Param("wordName") String wordName);

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.UNDEFINED_PATH)
    List<WordDefinitionDto> getUndefinedTeamWords();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.PENDING_PATH)
    List<WordDefinitionDto> getPromotionPendingTeamWords();

    //team dictionary promotion

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH)
    List<WordDefinitionDto> getGlobalTeamDictionary();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH)
    List<WordDefinitionDto> getGlobalCommunityDictionary();

    @RequestLine("GET "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.PENDING_PATH)
    List<PendingWordReferenceDto> getPendingCommunityWords();

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.ACCEPT_PATH)
    WordDefinitionDto acceptPendingWordIntoCommunityScope(PendingWordReferenceDto pendingWordReferenceDto);

    @RequestLine("POST "  + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.REJECT_PATH)
    void rejectPendingCommunityWord(PendingWordReferenceDto pendingWordReferenceDto);

    //organization dictionaries into books

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookReferenceDto createTeamBook(@Param("bookName") String bookName);

    @RequestLine("PUT " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookReferenceDto updateTeamBook(@Param("bookName") String bookName, RefactorBookInputDto refactorBookInputDto );

    @RequestLine("DELETE " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookReferenceDto archiveTeamBook(@Param("bookName") String bookName);


    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookReferenceDto createCommunityBook(@Param("bookName") String bookName);


    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH )
    List<BookReferenceDto> getTeamBooks();

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH )
    List<BookReferenceDto> getCommunityBooks();

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookDto getTeamBook(@Param("bookName") String bookName);

    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH + ResourcePaths.BOOK_PATH + "/{bookName}")
    BookDto getCommunityBook(@Param("bookName") String bookName);

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PULL_PATH)
    WordDefinitionDto pullWordIntoTeamBook(@Param("bookName") String bookName, @Param("wordName") String wordName);


    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.REMOVE_PATH)
    WordDefinitionDto removeWordFromTeamBook(@Param("bookName") String bookName, @Param("wordName") String wordName);


    @RequestLine("GET " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" )
    WordDefinitionWithDetailsDto getTeamBookWord(@Param("bookName") String bookName, @Param("wordName") String wordName);


    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.COMMUNITY_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}" + ResourcePaths.PULL_PATH)
    WordDefinitionDto pullWordIntoCommunityBook(@Param("bookName") String bookName, @Param("wordName") String wordName);

    //refactor definitions inside of books

    @RequestLine("POST " + ResourcePaths.DICTIONARY_PATH + ResourcePaths.SCOPE_PATH + ResourcePaths.TEAM_PATH +
            ResourcePaths.BOOK_PATH + "/{bookName}" + ResourcePaths.WORD_PATH + "/{wordName}")
    WordDefinitionDto refactorWordInsideTeamBook(@Param("bookName") String bookName, @Param("wordName") String wordName,
                                                 WordDefinitionInputDto wordDefinitionInputDto);



}
