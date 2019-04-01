package com.dreamscale.htmflow.api.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MasterAccountDto {

    private UUID masterAccountId;
    private String masterEmail;
    private String fullName;
}
