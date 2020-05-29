package com.dreamscale.gridtime.api.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RootLoginInputDto {

    private String username;
    private String password;
}
