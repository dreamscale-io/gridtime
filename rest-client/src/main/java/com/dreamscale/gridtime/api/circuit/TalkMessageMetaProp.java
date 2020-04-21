package com.dreamscale.gridtime.api.circuit;

import lombok.Getter;

@Getter
public enum TalkMessageMetaProp {
    FROM_MEMBER_ID("from.member.id"),
    FROM_USERNAME("from.username"),
    FROM_FULLNAME("from.fullname");

    private String propName;

    TalkMessageMetaProp(String propName) {
        this.propName = propName;
    }
}
