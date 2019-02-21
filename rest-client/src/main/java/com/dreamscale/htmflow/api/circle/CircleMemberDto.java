package com.dreamscale.htmflow.api.circle;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CircleMemberDto {

    UUID memberId;
    String fullName;
    String shortName;
}
