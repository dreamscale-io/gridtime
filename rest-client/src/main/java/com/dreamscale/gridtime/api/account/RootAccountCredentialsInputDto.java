package com.dreamscale.gridtime.api.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RootAccountCredentialsInputDto {

    private String email;
    private String password;

    //optional
    private String invitationKey;

    public RootAccountCredentialsInputDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
