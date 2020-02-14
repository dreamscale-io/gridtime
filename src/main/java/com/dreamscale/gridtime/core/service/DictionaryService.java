package com.dreamscale.gridtime.core.service;

import com.dreamscale.gridtime.api.dictionary.DictionaryBookDto;
import com.dreamscale.gridtime.api.dictionary.PendingTagReferenceDto;
import com.dreamscale.gridtime.api.dictionary.TagDefinitionDto;
import com.dreamscale.gridtime.api.dictionary.TagRefactorInputDto;
import com.dreamscale.gridtime.api.spirit.*;
import com.dreamscale.gridtime.core.domain.member.TorchieTombstoneEntity;
import com.dreamscale.gridtime.core.mapper.DtoEntityMapper;
import com.dreamscale.gridtime.core.mapper.MapperFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DictionaryService {


    @Autowired
    TimeService timeService;

    @Autowired
    private MapperFactory mapperFactory;
    private DtoEntityMapper<TorchieTombstoneDto, TorchieTombstoneEntity> torchieTombstoneMapper;

    @PostConstruct
    private void init() {
        torchieTombstoneMapper = mapperFactory.createDtoEntityMapper(TorchieTombstoneDto.class, TorchieTombstoneEntity.class);
    }

    public TagDefinitionDto getDefinition(UUID organizationId, UUID id, String tagName) {
        return null;
    }

    public TagDefinitionDto promoteDefinition(UUID organizationId, UUID id, String tagName) {
        return null;
    }

    public TagDefinitionDto refactorDefinition(UUID organizationId, UUID id, String tagName, TagRefactorInputDto tagRefactorInputDto) {

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
