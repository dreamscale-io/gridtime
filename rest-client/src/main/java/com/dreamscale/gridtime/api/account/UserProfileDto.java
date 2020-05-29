package com.dreamscale.gridtime.api.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private UUID rootAccountId;
    private String rootEmail;
    private String rootUsername;

    private String orgEmail;
    private String orgUsername;

    private String fullName;
    private String displayName;

}
