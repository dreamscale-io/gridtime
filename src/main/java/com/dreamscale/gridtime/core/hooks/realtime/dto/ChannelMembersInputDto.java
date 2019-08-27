package com.dreamscale.gridtime.core.hooks.realtime.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChannelMembersInputDto {

    List<MemberInputDto> channelMembers;
}


