package com.dreamscale.gridtime.core.hooks.jira.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JiraUserDto {
    String self;
    String accountId;
    String accountType;
    String emailAddress;
    Map<String, String> avatarUrls;

    String displayName;
    boolean active;
    String timeZone;
    String locale;
}
