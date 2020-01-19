package com.dreamscale.gridtime.resources;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.dictionary.DictionaryCloudDto;
import com.dreamscale.gridtime.api.dictionary.DictionaryPatternDto;
import com.dreamscale.gridtime.core.domain.member.OrganizationMemberEntity;
import com.dreamscale.gridtime.core.security.RequestContext;
import com.dreamscale.gridtime.core.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(path = ResourcePaths.DICTIONARY_PATH, produces = org.springframework.http.MediaType.APPLICATION_JSON_VALUE)
public class DictionaryResource {

    @Autowired
    OrganizationService organizationService;


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.HASHTAG_PATH + "/{tag}")
    public DictionaryPatternDto getHashtag(@PathVariable("tag") String hashTag) {
        RequestContext context = RequestContext.get();
        log.info("getHashtag, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return null;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping(ResourcePaths.HASHTAG_PATH + "/{tag}" + ResourcePaths.CLOUD_PATH + ResourcePaths.DEPTH_PATH + "/{depth}")
    public DictionaryCloudDto getHashtagCloud(@PathVariable("tag") String hashTag, int depth) {
        RequestContext context = RequestContext.get();
        log.info("getHashtagCloud, user={}", context.getMasterAccountId());

        OrganizationMemberEntity invokingMember = organizationService.getDefaultMembership(context.getMasterAccountId());

        return null;
    }


}
