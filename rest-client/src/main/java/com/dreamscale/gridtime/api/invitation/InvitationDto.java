package com.dreamscale.gridtime.api.invitation;

import com.dreamscale.gridtime.api.account.SimpleStatusDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationDto {

    String ticketType;

    SimpleStatusDto status;
}
