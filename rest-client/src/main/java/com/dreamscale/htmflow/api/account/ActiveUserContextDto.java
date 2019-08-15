package com.dreamscale.htmflow.api.account;

import com.dreamscale.htmflow.api.status.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActiveUserContextDto {

    UUID organizationId;
    UUID memberId;
    UUID teamId;

}
