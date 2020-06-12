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

    //required
    private String email;
    private String password;

    //optional
    private String username;
    private String fullName;
    private String displayName;

    public RootAccountCredentialsInputDto(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
