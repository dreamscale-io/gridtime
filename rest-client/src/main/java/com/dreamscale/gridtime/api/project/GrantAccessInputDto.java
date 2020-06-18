package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GrantAccessInputDto {

    private GrantType grantType;
    private UUID grantTo;
}
