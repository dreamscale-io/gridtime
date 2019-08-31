package com.dreamscale.gridtime.api.network;

import com.dreamscale.gridtime.api.account.UserContextDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberChannelsDto {

    private UserContextDto userContext;

    private List<UUID> listeningToChannels;

}

