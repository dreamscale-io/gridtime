package com.dreamscale.gridtime.core.domain.member.json;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PairingMemberList {

    List<Member> memberList;
}
