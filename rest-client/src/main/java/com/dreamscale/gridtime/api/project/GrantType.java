package com.dreamscale.gridtime.api.project;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public enum GrantType {

    //Note, these are mapped to DB enum entries
    MEMBER, TEAM;
}
