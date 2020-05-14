package com.dreamscale.gridtime.client;

import com.dreamscale.gridtime.api.ResourcePaths;
import com.dreamscale.gridtime.api.account.EmailInputDto;
import com.dreamscale.gridtime.api.organization.TeamMemberDto;
import feign.Headers;
import feign.RequestLine;

import java.util.List;

@Headers({
        "Content-Type: application/json",
        "Accept: application/json",
})
public interface MemberClient {

    @RequestLine("GET " + ResourcePaths.MEMBER_PATH + ResourcePaths.ME_PATH)
    TeamMemberDto getMe();

    @RequestLine("GET " + ResourcePaths.MEMBER_PATH + ResourcePaths.BY_PATH + ResourcePaths.EMAIL_PATH)
    TeamMemberDto findMemberWithEmail(EmailInputDto emailInputDto);
}
