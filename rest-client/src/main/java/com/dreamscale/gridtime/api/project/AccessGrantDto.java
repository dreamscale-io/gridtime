package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AccessGrantDto {

    String grantType;
    UUID grantedToId;
    String grantedToName;
}
